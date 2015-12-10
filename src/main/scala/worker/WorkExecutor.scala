package worker

import akka.actor.Actor

class WorkExecutor extends Actor {

  def receive = {
    case n: Int =>
      val n2 = n * n
      val result = s"$n * $n = $n2"
      sender() ! Worker.WorkComplete(result)
    case text: String =>
      Thread.sleep(3000)
      sender() ! Worker.WorkComplete(text.split("""\s+""").filter(_.matches("""\w+""")).map(_.toUpperCase).length)
  }

}