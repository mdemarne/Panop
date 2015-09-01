package panop

import akka.actor._

/**
 * Master controller for Panop search run.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem, var maxSlaves: Int = 200) extends Actor with ActorLogging {
  import com._

  /* Stacks */

  private var urls = List[Search]()
  private var foundLinks = Set[String]()
  private var results = List[Result]()
  // TODO: in the future, this could be done using Akka pools.
  private var slaves: List[ActorRef] = ((0 until maxSlaves + 1) map (ii => asys.actorOf(Props(new Slave)))).toList
  private var nbMissed = 0

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
    case res @ Result(search, matches, links) =>
      /* Getting proper search mode */
      val mw = new ModeWrapper(search.query.mode)
      import mw._
      /* Saving results */
      if (res.isPositive) results :+= res
      /* Saving links, filtered based on duplicates */
      val filteredLinks = links filter (link => !foundLinks.contains(link))
      foundLinks ++= filteredLinks
      urls = urls ::++ (filteredLinks map (l => search.copy(url = Url(l, search.url.depth + 1)))).toList
      log.debug(s"${search.url.link} done, found ${filteredLinks.size} new urls, ${if (res.isPositive) "[MATCHES]" else ""}")
      /* Restarting on urls */
      slaves :+= sender
      startRound

    /* If a slave has failed to fetch one page, this one will be requeued */
    case Failed(search) =>
      /* Getting proper search mode */
      val mw = new ModeWrapper(search.query.mode)
      import mw._
      // TODO: there should be some notion of repeated failure, and such URLs could be ignored at some point.
      if (search.coTentatives < Settings.defMaxCoTentatives) urls = urls ::+ search.copy(coTentatives = search.coTentatives + 1)
      else nbMissed += 1
      slaves :+= sender
      startRound

    case AskProgress => sender ! AswProgress(progress, nbExplored, foundLinks.size, results.size, nbMissed)
    case AskResults => sender ! AswResults(results)
  }

  /* Helpers */

  private def nbExplored = foundLinks.size - urls.size
  private def progress = nbExplored.toDouble / foundLinks.size.toDouble

  private def displayProgress = {

  }

  /** Start all available slaves on all available queued urls */
  private def startRound = {
    if (!urls.isEmpty) {
      val tpls = (slaves zip urls)
      slaves = slaves.drop(tpls.size)
      urls = urls.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }

  }
}