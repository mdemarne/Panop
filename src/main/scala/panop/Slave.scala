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
          val linkPattern = "href=(\"|\')[^\"\']+(\"|\')".r
          val linkPrefix = url.link.split("/").take(3).mkString("/") // TODO: this is uggly
          def removeHash(str: String) = str match {
            case "#" => ""
            case s if s.contains("#") && !s.forall(_ == "#") => s.split("#").init.mkString
            case _ => str
          }
          val links = (linkPattern.findAllIn(body).map(_.drop(6).dropRight(1)) map { str =>
            if (str.startsWith("http")) removeHash(str)
            else linkPrefix + (if(str.startsWith("/")) "" else "/") + removeHash(str)
          }).toSet
          val filteredLinks = if (query.linkPrefix.isEmpty) links else {
            links filter (link => link.startsWith(query.linkPrefix.get))
          }
          val isPositive = query.matches(body)
          sender ! Result(search, isPositive, filteredLinks)
        case Failure(err) =>
          log.error(s"Could not get data for $url")
          log.error(err.getMessage)
          sender ! Failed(search)
      }
  }
}