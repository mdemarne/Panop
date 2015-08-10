package panop

import akka.actor._
/**
 * Master controller for one search run of Panop
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem) extends Actor with ActorLogging {
  import Com._

  /* Parameters */
  private var maxSlaves: Int = 500

  /* Stacks */
  private var urls = List[Search]()
  private var foundLinks = Set[String]()
  private var results = List[Result]()
  private var slaves: List[ActorRef] = ((0 until maxSlaves) map (ii => asys.actorOf(Props(new Slave)))).toList

  private def explored = foundLinks.size - urls.size
  private def progress = explored.toDouble / foundLinks.size.toDouble

  def receive = {
    case StartSearch(url, query) =>
      val head = slaves.head
      slaves = slaves.tail
      foundLinks += url.link
      urls +:= Search(url, query)
      startRound

    case DisplayResults => // TODO: checkout for log here not println
      println("---------------------------------------------")
      println(s"Progress: $progress (explored $explored over ${foundLinks.size} links).")
      println(s"Found ${results.length} matches.")
      println(s"${slaves.size} slaves idle (${maxSlaves - slaves.size} active).")
      println("Displaying results...")
      results.sortBy(_.search.url.link.size) foreach (r => println("\t" + r.search.url.link)) // TODO: filter by query
      println("---------------------------------------------")

    case res @ Result(search, isPositive, links) =>
      /* Saving results */
      if (isPositive) results :+= res
      /* Saving links */
      if (search.url.depth < search.query.maxDepth) {
        val properLinks = links filter (search.query.ignoredFileExtensions.findFirstMatchIn(_).isEmpty)
        val filteredLinks = properLinks filter (link => !foundLinks.contains(link))
        foundLinks ++= filteredLinks
        urls ++= filteredLinks map (l => search.copy(url = Url(l, search.url.depth + 1)))
        log.info(s"${search.url.link} done, found ${filteredLinks.size} new urls, ${if (isPositive) "matches" else "does not match"}")
      }
      /* Restarting on urls */
      slaves :+= sender
      startRound

    case Failed(search) =>
      urls :+= search
      slaves :+= sender
      startRound
  }

  private def startRound = {
    if (!urls.isEmpty) { // TODO: avoid searching multiple time the same URL
      val tpls = (slaves zip urls) 
      slaves = slaves.drop(tpls.size)
      urls = urls.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }

  }
}