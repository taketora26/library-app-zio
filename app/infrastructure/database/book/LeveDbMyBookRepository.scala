package infrastructure.database.book

import domain.book.MyBook
import domain.book.MyBookRepository.Service
import org.iq80.leveldb.{DB, DBIterator}
import org.iq80.leveldb.impl.Iq80DBFactory.{asString, bytes}
import play.api.libs.json.{JsError, Json}
import zio.Task

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
