package launcher

import launcher.Logger.AppLogger
import zio.{ZEnv, ZLayer}

package object components {

  import domain.book._

  type AppContext = MyBookRepository with AppLogger

  object AppContext {
    val live: ZLayer[ZEnv, Throwable, AppContext] = AppLogger.live >>> MyBookRepository.live.passthrough
  }
}
