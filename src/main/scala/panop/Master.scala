package panop

import akka.actor._
/**
 * Master controller for one search run of Panop
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem) extends Actor with ActorLogging {
  import Com._

  /* Parameters */
  private var maxSlaves: Int = 200

  /* Stacks */
  private var urls = List[Search]()
  private var foundLinks = List[String]()
  private var results = List[Result]()

  /* Pool */
  private var slaves = (0 until maxSlaves) map (ii => asys.actorOf(Props(new Slave)))

  def receive = {
    case StartSearch(url, query) =>
      val head = slaves.head
      slaves = slaves.tail
      head ! Search(url, query)

    case DisplayResults =>
      log.info("---------------------------------------------")
      log.info("Displaying results...")
      results foreach (r => log.info("\t" + r.search.url.link)) // TODO: filter by query
      log.info("---------------------------------------------")

    case res @ Result(search, isPositive, links) =>
      /* Saving results */
      if (isPositive) {
        results :+= res
        log.info(s"Page $search matches.")
      }
      /* Saving links */
      if (search.url.depth < search.query.maxDepth) {
        val properLinks = links filter (!_.endsWith(search.query.ignoredFileExtensions))
        val filteredLinks = properLinks filter (url => !foundLinks.contains(url))
        foundLinks :::= filteredLinks
        urls :::= filteredLinks map (l => search.copy(url = Url(l, search.url.depth + 1)))
      }
      /* Restarting on urls */
      slaves +:= sender
      if (!urls.isEmpty) { // TODO: avoid searching multiple time the same URL
        val tpls = (slaves zip urls) 
        tpls foreach { tpl =>
          slaves = slaves.filter(_ != tpl._1)
          tpl._1 ! tpl._2
        }
        urls = urls.drop(tpls.size)
      }
  }
}