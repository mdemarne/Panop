package panop

import akka.actor._
import scala.util.{Try, Success, Failure}

import scalaj.http._

/**
 * Extracts data and do a local search once at a time.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
 // TODO: allow "focus" on some part of a page only
class Slave extends Actor with ActorLogging {
  import Com._

  def receive = {
    case search @ Search(url, query, _) =>
      Try(Http(url.link).asString) match {
        case Success(res) =>
          val linkPattern = "href=\"[^\"]+\"".r
          val linkPrefix = url.link.split("/").init.mkString("/")
          val links = linkPattern.findAllIn(res.body).map(_.drop(6).dropRight(1)).toList map { str =>
            if (str.startsWith("http")) str
            else linkPrefix + "/" + str
          }
          val isPositive = query.matches(res.body)
          log.info(s"${url.link} done, found ${links.size} urls, ${if (isPositive) "matches" else "does not match"}")
          sender ! Result(search, isPositive, links)
        case Failure(err) =>
          log.error(s"Could not get data for $url")
          log.error(err.getMessage)
      }
  }

}