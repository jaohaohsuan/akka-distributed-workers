package worker

import akka.actor.{ActorSystem, AddressFromURIString, Props, RootActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.japi.Util.immutableSeq
import com.typesafe.config.{Config, ConfigFactory}
import protocol.HostIP
import scala.concurrent.duration._
import scala.language.implicitConversions

object Main extends App {

    NodeConfig parse args match {
      case Some(x@NodeConfig(Some(port), canStore, _, Nil)) =>
        startBackend(x.`akka.cluster.seed-nodes`, port)(canStore)

      case Some(x@NodeConfig(None, false, Nil, _)) =>
        startWorker(x.`akka.contact-points`, 0)
      case _ =>
    }

  def workTimeout = 10.seconds

  def startBackend(extra: Config, port: Int)(startStore: Boolean): Unit = {

    val roles = List("backend", "store").filter { role => role != "store" | startStore }.mkString(",")

    val content = s"""
       |akka.cluster.roles = [ $roles ]
       |akka.remote.netty.tcp.port=$port
       |akka.remote.netty.tcp.hostname=${HostIP.load()}
     """.stripMargin

    val conf = ConfigFactory.parseString(content).withFallback(extra).withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", conf)

    system.actorOf(Props(classOf[Configurator]), "conf")

    if(startStore) {
      system.actorOf(Props[LeveldbJournalListener])
    }
  }

  /*def startFrontend(port: Int): Unit = {
    val conf = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", conf)
    val frontend = system.actorOf(Props[Frontend], "frontend")
    system.actorOf(Props(classOf[WorkProducer], frontend), "producer")
    system.actorOf(Props[WorkResultConsumer], "consumer")
  }*/

  def startWorker(extra: Config, port: Int): Unit = {

    val content = s"""akka.remote.netty.tcp.port=$port
                     |akka.remote.netty.tcp.hostname=${HostIP.load()}
     """.stripMargin

    // load worker.conf
    val conf = ConfigFactory.parseString(content).
      withFallback(ConfigFactory.load("worker"))

    val system = ActorSystem("WorkerSystem", conf)

    val initialContacts = immutableSeq(extra.getStringList("contact-points")).map {
      case AddressFromURIString(addr) ⇒ RootActorPath(addr) / "system" / "receptionist"
    }.toSet

    val clusterClient = system.actorOf(
      ClusterClient.props(
        ClusterClientSettings(system)
          .withInitialContacts(initialContacts)),
      "clusterClient")

    system.actorOf(Worker.props(clusterClient, Props[WorkExecutor]), "worker")
  }
}

object ImplicitPrint {

  implicit def toLogging[T](a: T) = WrappedLog[T](a)

  case class WrappedLog[T](a: T) {
    def println(f: T ⇒ String = _.toString): T = {
      Console.println(f(a))
      a
    }
    def print(f: T ⇒ String = _.toString): T = {
      Console.print(f(a))
      a
    }
  }
}
