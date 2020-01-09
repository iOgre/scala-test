package example

import cats.implicits._
import zio.duration._
import zio.interop.catz._
import zio.{ZEnv, ZIO, blocking, clock}


case object GreenServiceTimeout extends RuntimeException
case object BlueServiceTimeout extends RuntimeException

object Main extends zio.App {

  def main(ids: List[Int])(implicit runtime: zio.Runtime[ZEnv]): ZIO[ZEnv, Throwable, BlackResult] =
    for {
      // Если сервис RED недоступен, то надо подставить значение по умолчанию: RedAdapter.defaultResult(id)
      red <- ids.traverse { id =>
        blocking
          .effectBlocking(RedAdapter.run(id))
          .handleError(_ => RedAdapter.defaultResult(id))
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

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      before <- clock.currentDateTime
      _ <- console.putStrLn(before.toString)
      runtime <- ZIO.runtime[zio.ZEnv]
      eitherResult <- main(1.to(10).toList)(runtime).either
      _ <- console.putStrLn(eitherResult.toString)
      after <- clock.currentDateTime
      _ <- console.putStrLn(after.toString)
    } yield 0
}
