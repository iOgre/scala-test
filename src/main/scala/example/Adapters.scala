package example

import scala.concurrent.{ExecutionContext, Future}

object Util {
  def cpuTask(name: String): String = s"$name ${Thread.currentThread().getName}"
  def blockingIO(name: String): String = {
    Thread.sleep(1)
    if (System.nanoTime() % 10 == 0) throw new RuntimeException(s"$name ALERT")
    cpuTask(name)
  }
}

final case class RedResult(id: Int, value: String)
object RedAdapter {
  def defaultResult(id: Int): RedResult = RedResult(id, "RED DEFAULT")
  def run(id: Int): RedResult = RedResult(id, Util.blockingIO("RED"))
}

final case class GreenResult(red: List[RedResult], value: String)
object GreenAdapter {
  // Почему здесь не стоит использовать ExecutionContext.global
  def run(red: List[RedResult])(blockingEc: ExecutionContext): Future[GreenResult] =
    Future(GreenResult(red, Util.blockingIO("GREEN")))(blockingEc)
}

final case class BlueResult(green: GreenResult, value: String)
object BlueAdapter {

  def run(green: GreenResult, callback: BlueResult => Unit): Unit = {
    val t = new Thread {
      override def run(): Unit = {
        // Если хочется обработать ситуацию ошибки в финализаторе, то раскомментируй строчки ниже
        //      if (System.nanoTime() % 5 == 0) {
        callback(BlueResult(green, Util.cpuTask("BLUE")))
        println(s"FINALIZE BLUE ${Thread.currentThread().getName}")
        //      } else {
        //        println("Oops")
        //      }
      }
    }
    t.run()
  }
}

final case class BlackResult(blue: BlueResult, value: String)
object BlackAdapter {
  class BlackResource(id: Long) {
    def run(blue: BlueResult): BlackResult =
      BlackResult(blue, Util.blockingIO("BLACK"))
    def destroy(): Unit =
      println(s"FINALIZE BLACK $id ${Thread.currentThread().getName}")

  }
  def init(): BlackResource = new BlackResource(System.nanoTime() % 10)
}
