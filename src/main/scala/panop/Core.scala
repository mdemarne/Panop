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
  private var depth: Int = 0
  private var query: Option[Query] = None

  /* Stacks */
  private var urls = List[Url]()
  private var results = List[Result]()

  /* Pool */
  private var slaves = (0 until maxSlaves) map (ii => sys.actorOf(Props[Slave], s"slave$ii"))

  def receive = {
    case StartSearch(url, query, depth) => ??? // TODO
    case DisplayResults => ??? // TODO
  }
}