package api

import java.util.UUID
import akka.actor.ActorRef
import akka.pattern._
import akka.cluster.singleton.{ClusterSingletonProxySettings, ClusterSingletonProxy}
import akka.util.Timeout
import worker.{Master, Work}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object JobProcessor {
  case object Ok
  case object NotOk
}
/**
  * Created by henry on 12/6/15.
  */
trait JobProcessor {

  import JobProcessor._

  def masterProxy: ActorRef
  implicit def executor: ExecutionContext

  def nextWorkId(): String = UUID.randomUUID().toString

  def sentJob(n: Any) = {
    implicit val timeout = Timeout(5.seconds)
    val work = Work(nextWorkId(), n)
    (masterProxy ? work) map {
      case Master.Ack(_) => Ok
    } recover { case _ => NotOk }
  }
}
