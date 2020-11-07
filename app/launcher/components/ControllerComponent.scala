package launcher.components

import interfaces.rest.controllers.MyBookController
import interfaces.web.controllers.HomeController
import play.api.BuiltInComponentsFromContext
import zio.ZLayer

trait ControllerComponent {
  this: BuiltInComponentsFromContext =>

  implicit val appContext: ZLayer[zio.ZEnv, Throwable, AppContext]

  lazy val homeController   = new HomeController(controllerComponents)
  lazy val myBookController = new MyBookController(controllerComponents)

}
