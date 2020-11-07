package domain

import launcher.Logger.AppLogger
import play.api.libs.json.{JsError, Json}
import zio._
import org.iq80.leveldb.impl.Iq80DBFactory.{asString, bytes, factory}
import java.io.File

import org.iq80.leveldb.{DB, DBIterator, Options}

package object book {

  case class MyBook(id: String, Name: String)

  object MyBook {
    implicit val format = Json.format[MyBook]
  }

  // myBookパッケージオブジェクトで良さそう
  type MyBookRepository = Has[MyBookRepository.Service]

  object MyBookRepository {

    trait Service {
      def list(): Task[Seq[MyBook]]
      def getById(id: String): Task[Option[MyBook]]
      def save(myBook: MyBook): Task[Unit]
      def delete(id: String): Task[Unit]
    }

    def list(): RIO[MyBookRepository, Seq[MyBook]]                 = ZIO.accessM(_.get.list())
    def getById(id: String): RIO[MyBookRepository, Option[MyBook]] = ZIO.accessM(_.get.getById(id))
    def save(myBook: MyBook): RIO[MyBookRepository, Unit]          = ZIO.accessM(_.get.save(myBook))
    def delete(id: String): RIO[MyBookRepository, Unit]            = ZIO.accessM(_.get.delete(id))

    val live: ZLayer[AppLogger, Throwable, MyBookRepository] = ZLayer.fromFunctionManaged(mix =>
      ZManaged
        .make(
          AppLogger.info("Opening level DB at targetleveldb") *>
          Task {
            factory.open(
              new File("target/leveldb").getAbsoluteFile,
              new Options().createIfMissing(true)
            )
          }
        )(db => AppLogger.info("Closing level DB at target/leveldb") *> UIO(db.close()))
        .map(db => new LeveDbMyBookRepository(db))
        .provide(mix)
    )

    // TODO infrastructure　へ切り離す
    class LeveDbMyBookRepository(db: DB) extends Service {

      def parseJson(str: String): Task[MyBook] =
        Task(Json.parse(str)).flatMap { json =>
          json
            .validate[MyBook]
            .fold(
              err => Task.fail(new RuntimeException(s"Error parsing myBook: ${Json.stringify(JsError.toJson(err))}")),
              ok => Task.succeed(ok)
            )
        }

      def listAll(iterator: DBIterator): Task[List[MyBook]] =
        for {
          hasNext <- Task(iterator.hasNext)
          value <- if (hasNext) {
                    for {
                      nextValue <- Task(iterator.next())
                      myBook    <- parseJson(asString(nextValue.getValue))
                      n         <- listAll(iterator)
                    } yield myBook :: n
                  } else {
                    Task(List.empty[MyBook])
                  }
        } yield value

      override def list(): Task[Seq[MyBook]] = listAll(db.iterator())

      override def getById(id: String): Task[Option[MyBook]] =
        for {
          stringValue <- Task(asString(db.get(bytes(id))))
          myBook <- if (stringValue != null) {
                     parseJson(stringValue).map(Option.apply)
                   } else Task.succeed(Option.empty[MyBook])
        } yield myBook

      override def save(myBook: MyBook): Task[Unit] =
        Task {
          val stringMyBook = Json.stringify(Json.toJson(myBook))
          db.put(bytes(myBook.id), bytes(stringMyBook))
        }

      override def delete(id: String): Task[Unit] = Task(db.delete(bytes(id)))

    }
  }
}
