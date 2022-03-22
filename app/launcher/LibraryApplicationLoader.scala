package launcher

import launcher.components._
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import router.Routes
import zio.{Runtime, ZEnv, ZIO, ZLayer}

class LibraryApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new LibraryComponents(context).application
}

class LibraryComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with ControllerComponent
    with play.filters.HttpFiltersComponents {

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  implicit val runtime: Runtime[ZEnv] = Runtime.default

  type Eff[A] = ZIO[ZEnv, Throwable, A]
  import zio.interop.catz._

  implicit val (appContext: ZLayer[zio.ZEnv, Throwable, AppContext], release: Eff[Unit]) =
    Runtime.default.unsafeRun(AppContext.live.memoize.toResource[Eff].allocated)
  applicationLifecycle.addStopHook(() => Runtime.default.unsafeRunToFuture(release))

  lazy val router: Router = {
    lazy val prefix = "/"
    new Routes(httpErrorHandler, myBookController, prefix)
  }

}
