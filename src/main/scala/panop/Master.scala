package panop

import akka.actor._

import scala.collection.immutable.HashSet

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Master controller for Panop search run.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem, var maxSlaves: Int = 200) extends Actor with ActorLogging {
  import com._

  /* Stacks */

  @volatile private var urls = List[Search]()
  @volatile private var foundLinks = HashSet[String]()
  @volatile private var results = List[Result]()
  // TODO: in the future, this could be done using Akka pools.
  @volatile private var slaves: List[ActorRef] = ((0 until maxSlaves + 1) map (ii => asys.actorOf(Props(new Slave)))).toList
  @volatile private var nbMissed = 0

  /* Main */

  def receive = {
    /* Starting a specific query on an original URL (which depth should be 0) */
    case Search(url, query, _) =>
      val head = slaves.head
      slaves = slaves.tail
      foundLinks += url.link
      urls +:= Search(url, query)
      startRound

    /* Process a result coming from a slave */
    case res: Result =>
      slaves :+= sender /* Sender is now idle */
      Future { this.reduce(res) }

    /* If a slave has failed to fetch one page, this one will be requeued */
    case Failed(search) =>
      slaves :+= sender /* Sender is now idle */
      Future { this.bounce(search) }

    case AskProgress => sender ! AswProgress(progress, nbExplored, foundLinks.size, results.size, nbMissed)
    case AskResults => sender ! AswResults(results)
  }

  /* Helpers */

  @volatile private def bounce(search: Search) = urls.synchronized {
    /* Getting proper search mode */
    val mw = new ModeWrapper(search.query.mode)
    import mw._
    // TODO: there should be some notion of repeated failure, and such URLs could be ignored at some point.
    if (search.coTentatives < Settings.defMaxCoTentatives) urls = urls ::+ search.copy(coTentatives = search.coTentatives + 1)
    else nbMissed += 1
    this.startRound
  }
  @volatile private def reduce(res: Result) = urls.synchronized {
    /* Getting proper search mode */
    val mw = new ModeWrapper(res.search.query.mode)
    import mw._
    /* Saving results */
    if (res.isPositive) results :+= res
    /* Saving links, filtered based on duplicates */
    urls = urls ::++ ((res.links -- foundLinks) map (l => res.search.copy(url = Url(l, res.search.url.depth + 1)))).toList
    foundLinks ++= res.links
    log.debug(s"${res.search.url.link} done, found ${res.links.size} urls, ${if (res.isPositive) "[MATCHES]" else ""}")
    /* Restarting on urls */
    this.startRound
  }
  /** Start all available slaves on all available queued urls */
  @volatile private def startRound = {
    if (!urls.isEmpty) {
      val tpls = (slaves zip urls)
      slaves = slaves.drop(tpls.size)
      urls = urls.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }
  }

  private def nbExplored = foundLinks.size - urls.size
  private def progress = nbExplored.toDouble / foundLinks.size.toDouble
}