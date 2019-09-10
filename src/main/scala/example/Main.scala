package example

import java.time.LocalDateTime
import java.util.concurrent.Executors

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Promise}

object Main extends App {
  private val blockingEc =
    ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  def run(ids: List[Int]): BlackResult = {
    // Если сервис RED недоступен, то надо подставить значение по умолчанию: RedAdapter.defaultResult(id)
    val red = ids.map(RedAdapter.run)

    val green = Await.result(GreenAdapter.run(red)(blockingEc), 10.seconds)

    val promise = Promise[BlueResult]()
    BlueAdapter.run(green, promise.success)
    val blue = Await.result(promise.future, 10.seconds)

    val blackResource = BlackAdapter.init()
    val black = blackResource.run(blue)
    blackResource.destroy()
    black
  }
  println(LocalDateTime.now())
  println(run(1.to(10).toList))
  println(LocalDateTime.now())
}
