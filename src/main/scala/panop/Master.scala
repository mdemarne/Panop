package panop

import akka.actor._
/**
 * Master controller for one search run of Panop
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem) extends Actor with ActorLogging {
  import com._

  /* Parameters */
  // TODO: remove hard coded
  private var maxSlaves: Int = 200 // TODO: Based on experiment, the JVM has difficulties to support more than 1000 slaves.

  /* Stacks */
  private var urls = List[Search]()
  private var foundLinks = Set[String]()
  private var results = List[Result]()
  // TODO: in the future, this could be done using simple Akka pools.
  private var slaves: List[ActorRef] = ((0 until maxSlaves) map (ii => asys.actorOf(Props(new Slave)))).toList

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

    /* Simply display the results on demand */
    case DisplayResults => // TODO: checkout for log here not log.info
      log.info("---------------------------------------------")
      log.info(s"Progress: $progress (explored $explored over ${foundLinks.size} links).")
      log.info(s"Found ${results.length} matches.")
      log.info(s"${slaves.size} slaves idle (${maxSlaves - slaves.size} active).")
      log.info("Displaying results...")
      results.sortBy(_.search.url.link.size) foreach (r => log.info("\t" + r.search.url.link)) // TODO: filter by query
      log.info("---------------------------------------------")

    /* Process a result coming from a slave */
    case res @ Result(search, isPositive, links) =>
      /* Saving results */
      if (isPositive) results :+= res
      /* Saving links, filtered based on duplicates */
      val filteredLinks = links filter (link => !foundLinks.contains(link))
      foundLinks ++= filteredLinks
      urls ++= filteredLinks map (l => search.copy(url = Url(l, search.url.depth + 1)))
      log.info(s"${search.url.link} done, found ${filteredLinks.size} new urls, ${if (isPositive) "[MATCHES]" else ""}")
      /* Restarting on urls */
      slaves :+= sender
      startRound

    /* If a slave has failed to fetch one page, this one will be requeued */
    case Failed(search) =>
      // TODO: there should be some notion of repeated failure, and such URLs could be ignored at some point.
      urls :+= search
      slaves :+= sender
      startRound
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