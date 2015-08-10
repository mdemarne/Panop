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
    case search @ Search(url, query) =>
      Try(Http(url.link).asString) match {
        case Success(res) =>
          val t1 = query.boundaries._1.findFirstMatchIn(res.body).map(_.after).getOrElse(res.body)
          val t2 = query.boundaries._2.findFirstMatchIn(t1).map(_.before).getOrElse(t1)
          val body = t2.toString
          val linkPattern = "href=\"[^\"]+\"".r
          val linkPrefix = url.link.split("/").init.mkString("/")
          val links = linkPattern.findAllIn(body).map(_.drop(6).dropRight(1)).toList map { str =>
            if (str.startsWith("http")) str
            else linkPrefix + "/" + str
          }
          val isPositive = query.matches(body)
          log.info(s"${url.link} done, found ${links.size} urls, ${if (isPositive) "matches" else "does not match"}")
          sender ! Result(search, isPositive, links)
        case Failure(err) =>
          log.error(s"Could not get data for $url")
          log.error(err.getMessage)
      }
  }

}