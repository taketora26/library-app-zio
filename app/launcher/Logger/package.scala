package launcher

import play.api.MarkerContext
import zio.{Has, UIO, URIO, ZIO, ZLayer}

package object Logger {

  type AppLogger = Has[AppLogger.Service]

  object AppLogger {
    import play.api.Logger

    trait Service {
      def info(message: => String)(implicit mc: MarkerContext): UIO[Unit]
      def debug(message: => String)(implicit mc: MarkerContext): UIO[Unit]
    }

    def info(message: => String)(implicit mc: MarkerContext): URIO[AppLogger, Unit]  = ZIO.accessM(_.get.info(message))
    def debug(message: => String)(implicit mc: MarkerContext): URIO[AppLogger, Unit] = ZIO.accessM(_.get.debug(message))

    // Zlayerは環境Rを作るためのレシピを表すデータ型
    val live: ZLayer[Any, Nothing, AppLogger] = ZLayer.succeed(new ProdLogger())

    class ProdLogger(logger: Logger = Logger("application")) extends AppLogger.Service {
      override def info(message: => String)(implicit mc: MarkerContext): UIO[Unit]  = UIO(logger.info(message))
      override def debug(message: => String)(implicit mc: MarkerContext): UIO[Unit] = UIO(logger.debug(message))
    }

  }

}
