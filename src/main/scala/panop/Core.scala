package panop

import akka.actor._
/**
 * Core controller for one search run of Panop
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Core(sys: ActorSystem) extends Actor with ActorLogging {
  import Com._

  /* Parameters */
  private var maxSlaves: Int = 200
  private var maxDepth: Int = 0

  /* Stacks */
  private var urls = List[Search]()
  private var results = List[Result]()

  /* Pool */
  private var slaves = (0 until maxSlaves) map (ii => sys.actorOf(Props[Slave], s"slave$ii"))

  def receive = {
    case StartSearch(url, query, depth) =>
      maxDepth = depth

      val head = slaves.head
      slaves = slaves.tail
      head ! Search(url, query)

    case DisplayResults =>
      results foreach (r => log.info(r.search.url.link)) // TODO: filter by query

    case res @ Result(search, isPositive, links) =>
      /* Saving results */
      if (isPositive) {
        results :+= res
        log.info(s"Page $search matches.")
      }

      /* Saving links */
      if (search.url.depth < maxDepth) urls :::= links map (l => Search(Url(l, search.url.depth), search.query))
      
    // TODO: restart slaves if required
  }
}