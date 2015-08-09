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

  /* Stacks */
  private var urls = List[Search]()
  private var results = List[Result]()

  /* Pool */
  private var slaves = (0 until maxSlaves) map (ii => sys.actorOf(Props[Slave], s"slave$ii"))

  def receive = {
    case StartSearch(url, query, maxDepth) =>
      val head = slaves.head
      slaves = slaves.tail
      head ! Search(url, query, maxDepth)

    case DisplayResults =>
      results foreach (r => log.info(r.search.url.link)) // TODO: filter by query

    case res @ Result(search, isPositive, links) =>
      /* Saving results */
      if (isPositive) {
        results :+= res
        log.info(s"Page $search matches.")
      }
      /* Saving links */
      if (search.url.depth < search.maxDepth) urls :::= links map (l => search.copy(url = Url(l, search.url.depth + 1)))
      /* Restarting on urls */
      slaves +:= sender
      if (!urls.isEmpty) {
        (slaves zip urls) foreach { tpl =>
          slaves = slaves.filter(_ != tpl._1)
          tpl._1 ! tpl._2
        }
      }
  }
}