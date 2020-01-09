package example

import zio.{ZEnv, ZIO, clock, console}

object Main extends zio.App {
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      before <- clock.currentDateTime
      _ <- console.putStrLn(before.toString)
      eitherResult <- Program.main(1.to(10).toList).either
      _ <- console.putStrLn(eitherResult.toString)
      after <- clock.currentDateTime
      _ <- console.putStrLn(after.toString)
    } yield 0
}
