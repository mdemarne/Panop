package panop

import akka.actor._
import scala.util.{ Try, Success, Failure }

import scalaj.http._

/**
 * Extracts data and do a local search once at a time.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Slave extends Actor with ActorLogging {
  import com._

  def receive = {
    case search @ Search(url, query, _) =>
      Try(Http(url.link).asString) match {

        case Success(res) =>
          /* Get page boundaries */
          val t1 = query.boundaries._1.findFirstMatchIn(res.body)
            .map(_.after).getOrElse(res.body)
          val t2 = query.boundaries._2.findFirstMatchIn(t1)
            .map(_.before).getOrElse(t1)
          val body = t2.toString
          /* Check if the depth as been reached */
          val newLinks: Set[String] = url.depth match {
            case d if d == query.maxDepth => Set()
            case d =>
              /* get all links, append prefix if required */
              val linkPattern = "href=(\"|\')[^\"\']+(\"|\')".r
              val domain = url.link.split("/").take(3).mkString("/")
              // Generating all absolute link from local links */
              val absoluteLinks = (linkPattern.findAllIn(body)
                .map(_.drop(6).dropRight(1))
                .map { str =>
                if (str.startsWith("http")) removeAncres(str) // # not suported
                else domain + (
                  if (str.startsWith("/")) "" else "/"
                ) + removeAncres(str)
              }).toSet
              /* Check that all links are still in the required domain */
              val boundedLinks = if (query.domain.isEmpty) absoluteLinks else {
                absoluteLinks filter (link => link.startsWith(query.domain.get))
              }
              /* Remove all ignored link extensions */
              val properExtLinks = boundedLinks filter (
                query.ignoredFileExtensions.findFirstMatchIn(_).isEmpty)
              properExtLinks // Returning list of new links
          }
          /* Searching for page match */
          val matches = query.matches(body)
          /* Sending results */
          sender ! Result(search, matches, newLinks)

        case Failure(err) =>
          log.debug(s"Could not get data for $url")
          log.debug(err.getMessage)
          sender ! Failed(search)
      }
  }

  /* Remove all ancres from the HTML code. Currently not supported. */
  private def removeAncres(str: String) = str match {
    case "#" => ""
    case s if s.contains("#") && !s.forall(_ == "#") =>
      s.split("#").init.mkString
    case _ => str
  }
}
