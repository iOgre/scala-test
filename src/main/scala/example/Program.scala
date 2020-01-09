package example

import zio.{ZIO, blocking, ZEnv}
import zio.duration._

case object GreenServiceTimeout extends RuntimeException
case object BlueServiceTimeout extends RuntimeException

object Program {
  def main(ids: List[Int]): ZIO[ZEnv, Throwable, BlackResult] =
    for {
      // Если сервис RED недоступен, то надо подставить значение по умолчанию: RedAdapter.defaultResult(id)
      red <- ZIO.foreach(ids) { id =>
        blocking
          .effectBlocking(RedAdapter.run(id))
          .orElse(ZIO.effectTotal(RedAdapter.defaultResult(id)))
      }
      green <- blocking
        .blocking(ZIO.fromFuture(ec => GreenAdapter.run(red)(ec)))
        .timeout(10.seconds)
        .someOrFail(GreenServiceTimeout)
      blue <- ZIO
        .effectAsync[Any, Throwable, BlueResult] { callback =>
          BlueAdapter.run(green,
                          blueResult => callback(ZIO.succeed(blueResult)))
        }
        .timeout(10.seconds)
        .someOrFail(BlueServiceTimeout)
      black <- ZIO.bracket(ZIO.effect(BlackAdapter.init())) { r =>
        ZIO.effect(r.destroy()).either
      } { r =>
        blocking.effectBlocking(r.run(blue))
      }
    } yield black
}
