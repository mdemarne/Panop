package panop

import akka.actor._

/**
 * Master controller for one search run of Panop
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem, var maxSlaves: Int = 200) extends Actor with ActorLogging {
  import com._

  /* Stacks */
  private var urls = List[Search]()
  private var foundLinks = Set[String]()
  private var results = List[Result]()
  // TODO: in the future, this could be done using simple Akka pools.
  private var slaves: List[ActorRef] = ((0 until maxSlaves + 1) map (ii => asys.actorOf(Props(new Slave)))).toList

  private def explored = foundLinks.size - urls.size
  private def progress = explored.toDouble / foundLinks.size.toDouble

  def receive = {
    /* Starting a specific query on an original URL (which depth should be 0) */
    case Search(url, query) =>
      val head = slaves.head
      slaves = slaves.tail
      foundLinks += url.link
      urls +:= Search(url, query)
      startRound

    case DisplayProgress => displayProgress

    /* Simply display the results on demand */
    case DisplayResults =>
      displayProgress
      log.info("Displaying results...")
      results.sortBy(_.search.url.link.size) foreach (r => log.info("\t" + r.search.url.link + " [" + Query.printNormalForm(r.matches) + "]")) // TODO: filter by query
      log.info("---------------------------------------------")

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
      log.info(s"${search.url.link} done, found ${filteredLinks.size} new urls, ${if (res.isPositive) "[MATCHES]" else ""}") // TODO: change that to debug
      /* Restarting on urls */
      slaves :+= sender
      startRound

    /* If a slave has failed to fetch one page, this one will be requeued */
    case Failed(search) =>
      /* Getting proper search mode */
      val mw = new ModeWrapper(search.query.mode)
      import mw._
      // TODO: there should be some notion of repeated failure, and such URLs could be ignored at some point.
      urls = urls ::+ search
      slaves :+= sender
      startRound
  }

  private def displayProgress = {
    log.info("---------------------------------------------")
    log.info(s"Progress: $progress (explored $explored over ${foundLinks.size} links).")
    log.info(s"Found ${results.length} matches.")
    log.info(s"${slaves.size} slaves idle (${maxSlaves - slaves.size} active).")
    log.info("---------------------------------------------")
  }

  /** Start all available slaves on all available queued urls */
  private def startRound = {
    if (!urls.isEmpty) { // TODO: avoid searching multiple time the same URL
      val tpls = (slaves zip urls)
      slaves = slaves.drop(tpls.size)
      urls = urls.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }

  }
}