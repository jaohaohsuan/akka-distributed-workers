package worker

import akka.actor.PoisonPill
import akka.cluster.singleton.{ClusterSingletonManagerSettings, ClusterSingletonManager}
import scala.concurrent.duration._

class Configurator extends SetSharedLeveldbStore {

  import context.system

  def processReceive: Receive = {
    case LeveldbStoreRegistration(m) =>
      if(m.hasRole("backend")) {

        def workTimeout = 10.seconds

        system.actorOf(
          ClusterSingletonManager.props(
            Master.props(workTimeout),
            PoisonPill,
            ClusterSingletonManagerSettings(system).withRole("backend")
          ),
          "master")
      }
  }
}
