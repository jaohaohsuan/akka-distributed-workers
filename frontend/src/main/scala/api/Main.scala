package api

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.singleton.{ClusterSingletonProxySettings, ClusterSingletonProxy}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import protocol.HostIP
import worker.WorkResultConsumer

import scala.concurrent.ExecutionContext

object Main extends App {

  (WebConfig parse args).foreach { c =>

    val content = s"akka.remote.netty.tcp.hostname=${HostIP.load()}"
    val conf = ConfigFactory.parseString(content)
    .withFallback(c.`akka.cluster.seed-nodes`)
    .withFallback(ConfigFactory.load())

    new SendJobApi {

      implicit val system = ActorSystem("ClusterSystem", conf)
      implicit val executor: ExecutionContext = system.dispatcher
      implicit val materializer: ActorMaterializer = ActorMaterializer()

      val masterProxy: ActorRef = system.actorOf(
        ClusterSingletonProxy.props(
          settings = ClusterSingletonProxySettings(system).withRole("backend"),
          singletonManagerPath = "/user/master"
        ), name = "masterProxy")

      system.actorOf(Props[WorkResultConsumer], "consumer")

      val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", c.port)
      sys.addShutdownHook(system.terminate())
    }

  }
}