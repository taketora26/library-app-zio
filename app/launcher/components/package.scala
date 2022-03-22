package launcher

import infrastructure.database.book.LeveDbMyBookRepository
import launcher.Logger.AppLogger
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory.factory
import zio.{Task, UIO, ZEnv, ZLayer, ZManaged}

import java.io.File

package object components {

  import domain.book._

  type AppContext = MyBookRepository with AppLogger

  object AppContext {
    // ZLayerの合成。縦に合成をこなっている。（leveDbLiveはAppLogger.liveのエフェクトを利用）
    // passthroughはleveDbLiveの入力から通過するZLayerを返している。
    val live: ZLayer[ZEnv, Throwable, AppContext] = AppLogger.live >>> leveDbLive.passthrough
  }

  val leveDbLive: ZLayer[AppLogger, Throwable, MyBookRepository] = ZLayer.fromFunctionManaged(mix =>
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

}
