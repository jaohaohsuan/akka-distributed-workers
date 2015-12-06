package worker

import protocol.HostIP._
import protocol.HostIP
import com.typesafe.config.{Config, ConfigFactory}

import scala.language.implicitConversions

case class NodeConfig(backend: Option[Int] = None, leveldb: Boolean = false,
                      seedNodes: List[String] = Nil,
                      contactPoints: List[String] = Nil) {

  import ImplicitPrint._

  implicit def stringToConfig(value: String) = ConfigFactory.parseString(value).resolve()

  lazy val `akka.contact-points`: Config = contactPoints.map { addr =>
    s"""contact-points += "akka.tcp://ClusterSystem@${lookupNodeAddress(addr)}"""" }
  .mkString("\n").println()

  lazy val `akka.cluster.seed-nodes`: Config = backend.map { port => s"${HostIP.load()}:${port}" :: seedNodes }.getOrElse(Nil).map { addr =>
    s"""akka.cluster.seed-nodes += "akka.tcp://ClusterSystem@${lookupNodeAddress(addr)}"""" }
  .mkString("\n")
}

object NodeConfig {

  def parse(args: Seq[String]): Option[NodeConfig]= {

    val parser = new scopt.OptionParser[NodeConfig]("akka-distributed-workers"){
      opt[Int]('b', "backend") action { (x, c) =>
        c.copy(backend = Some(x)) } text("backend port")
      opt[Unit]("leveldb") action { (x, c) =>
        c.copy(leveldb = true) } text("persistence node")
      opt[Seq[String]]("seedNodes") action { (n, c) =>
        c.copy(seedNodes = c.seedNodes ++ n)
      } text "give a list of seed nodes like this: <ip>:<port> <ip>:<port>"
      opt[Seq[String]]("contactPoints") action { (n, c) =>
        c.copy(contactPoints = c.contactPoints ++ n)
      } text "give a list of contact-points like this: <ip>:<port> <ip>:<port>"
      checkConfig {
        case NodeConfig(None, _, _, Nil) => failure("invalid combination")
        case NodeConfig(None, _ ,Nil, _) => success
        case NodeConfig(_, _ ,_, Nil) => success
        case _ => failure("invalid combination")
      }
    }

    parser.parse(args, NodeConfig())
  }
}
