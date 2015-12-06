package api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import scala.util.{Failure, Success}


trait SendJobApi extends JobProcessor {

  import JobProcessor._

  val route =
    path("job"){
      get {
        complete {
          <h1>Post a job</h1>
        }
      } ~
      post {
        onComplete(sentJob(10)) {
          case Success(Ok) => complete(Created)
          case Success(NotOk) => complete(BadRequest)
          case Failure(ex)    => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      }
    }
}
