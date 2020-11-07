package interfaces.rest.controllers.dtos

import play.api.libs.json.Json

case class MyBookDto(name: String)
object MyBookDto {
  implicit val format = Json.format[MyBookDto]
}
