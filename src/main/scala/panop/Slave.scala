package panop

import akka.actor._
import scala.util.{Try, Success, Failure}

import scalaj.http._

/**
 * Extracts data and do a local search once at a time.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Slave extends Actor with ActorLogging {

  val originName = "Slave"

  import Com._

  def receive = {
    case Search(url, query) =>
      Try(Http(url.link).asString) match {
        case Success(resp) => ???
          // TODO: extract urls
          // TODO: check existence
        case Failure(err) =>
          log.error(s"Could not get data for $url")
          log.error(err.getMessage)
      }
  }

}