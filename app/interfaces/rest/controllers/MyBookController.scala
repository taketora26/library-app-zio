package interfaces.rest.controllers

import java.util.UUID

import domain.book.{MyBook, MyBookRepository}
import interfaces.rest.controllers.dtos.MyBookDto
import launcher.Logger.AppLogger
import launcher.components.AppContext
import play.api.libs.json.{JsError, JsResult, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import zio.{Task, ZIO}

class MyBookController(val controllerComponents: ControllerComponents)(implicit cxt: HttpContext[AppContext])
    extends BaseController {

  def list(): Action[AnyContent] = Action.asyncZio[AppContext] { _ =>
    for {
      _       <- AppLogger.debug(s"Listing all myBooks")
      myBooks <- MyBookRepository.list().mapError(e => InternalServerError(s"${e.getMessage}"))
    } yield Ok(Json.toJson(myBooks))
  }

  def getById(id: String) = Action.asyncZio[AppContext] { req =>
    for {
      _           <- AppLogger.debug(s"Looking for user $id")
      mayBeMyBook <- MyBookRepository.getById(id).mapError(_ => InternalServerError)
      myBook      <- ZIO.fromOption(mayBeMyBook).mapError(_ => NotFound(Json.obj("message" -> s"No myBook for $id")))
    } yield Ok(Json.toJson(myBook))
  }

  def create() = Action.asyncZio[AppContext](parse.json) { req =>
    val myBookParsed: JsResult[MyBookDto] = req.body.validate[MyBookDto]
    for {
      _      <- AppLogger.debug(s"Creating myBook")
      myBook <- ZIO.fromEither(myBookParsed.asEither).mapError(err => BadRequest(JsError.toJson(err)))
      id     <- Task(UUID.randomUUID().toString).mapError(_ => InternalServerError)
      _      <- MyBookRepository.save(MyBook(id, myBook.name)).mapError(_ => InternalServerError)
    } yield Ok(Json.toJson(myBook))
  }

  def update(id: String) = Action.asyncZio[AppContext](parse.json) { req =>
    val myBookParsed: JsResult[MyBook] = req.body.validate[MyBook]
    for {
      _           <- AppLogger.debug(s"Updating myBook $id")
      myBook      <- ZIO.fromEither(myBookParsed.asEither).mapError(err => BadRequest(JsError.toJson(err)))
      mayBeMyBook <- MyBookRepository.getById(id).mapError(_ => InternalServerError)
      _           <- ZIO.fromOption(mayBeMyBook).mapError(_ => BadRequest(Json.obj("message" -> s"MuBook $id should exists")))
      _           <- MyBookRepository.delete(id).mapError(_ => InternalServerError)
      _           <- MyBookRepository.save(MyBook(id, myBook.Name)).mapError(_ => InternalServerError)
    } yield Ok(Json.toJson(myBook))
  }

}
