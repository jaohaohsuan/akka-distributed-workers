package api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.heikoseeberger.akkahttpupickle.UpickleSupport
import scala.util.{Failure, Success}

case class Job(n: Int)
case class TextJob(n: String)

trait SendJobApi extends JobProcessor  {

  import JobProcessor._


  val route = {
    import UpickleSupport._
    path("job") {
      get {
        complete {
          <h1>Post a job</h1>
        }
      } ~
      post {

        entity(as[Job]) { case Job(n) =>
          onComplete(sentJob(n)) {
            case Success(Ok) => complete(Created)
            case Success(NotOk) => complete(BadRequest)
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        } ~ entity(as[TextJob]) { case TextJob(n) =>
          onComplete(sentJob(n)) {
            case Success(Ok) => complete(Created)
            case Success(NotOk) => complete(BadRequest)
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
  }
}
