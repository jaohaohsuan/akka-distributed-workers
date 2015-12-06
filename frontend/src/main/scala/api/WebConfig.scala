package api

import com.typesafe.config.{ConfigFactory, Config}
import protocol.HostIP
import protocol.HostIP._
import scala.language.implicitConversions

case class WebConfig(port: Int = 8080, seedNodes: List[String] = Nil) {

  implicit def stringToConfig(value: String): Config = ConfigFactory.parseString(value).resolve()

  lazy val `akka.cluster.seed-nodes`: Config = seedNodes.map { addr =>
    s"""akka.cluster.seed-nodes += "akka.tcp://ClusterSystem@${lookupNodeAddress(addr)}"""" }
  .mkString("\n")
}

object WebConfig {
  def parse(args: Seq[String]): Option[WebConfig]= {
    val parser = new scopt.OptionParser[WebConfig]("frontend"){
      opt[Int]("port") action { (x, c) =>
        c.copy(port = x) } text("RESTful api port")
      opt[Seq[String]]("seedNodes") action { (n, c) =>
        c.copy(seedNodes = c.seedNodes ++ n)
      } text "give a list of seed nodes like this: <ip>:<port> <ip>:<port>"
      checkConfig {
        case WebConfig(_, Nil) => failure("use seedNodes to join the cluster")
        case WebConfig(port, _) if port <= 0 => failure("invalid port try 7879, 8080, etc..")
        case _ => success
      }
    }

    parser.parse(args, WebConfig())
  }
}
