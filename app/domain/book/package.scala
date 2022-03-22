package domain

import play.api.libs.json.Json
import zio._

package object book {

  case class MyBook(id: String, Name: String)

  object MyBook {
    implicit val format = Json.format[MyBook]
  }

  /**
   * Hasは、effectの依存性（このMyBookRepository.Serviceを必要とするエフェクト）を表現するための型
   * Hasは++演算子を使用して組み合わせることができる
   * そして、複数のHasを結合したあとでも、 それぞれの環境を分離して取り出すことができる
   */
  type MyBookRepository = Has[MyBookRepository.Service]

  object MyBookRepository {

    trait Service {
      def list(): Task[Seq[MyBook]]
      def getById(id: String): Task[Option[MyBook]]
      def save(myBook: MyBook): Task[Unit]
      def delete(id: String): Task[Unit]
    }

    // accessMはエフェクトの環境にアクセスするメソッド
    def list(): RIO[MyBookRepository, Seq[MyBook]]                 = ZIO.accessM(_.get.list())
    def getById(id: String): RIO[MyBookRepository, Option[MyBook]] = ZIO.accessM(_.get.getById(id))
    def save(myBook: MyBook): RIO[MyBookRepository, Unit]          = ZIO.accessM(_.get.save(myBook))
    def delete(id: String): RIO[MyBookRepository, Unit]            = ZIO.accessM(_.get.delete(id))
  }

}
