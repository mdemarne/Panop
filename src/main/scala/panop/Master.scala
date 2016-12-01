package panop

import akka.actor._

import scala.collection.immutable.HashSet

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Master controller for Panop search run. Gathers all the results and schedule
 * next tests batches.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem, // TODO: ideally, vars should not be input here.
  var maxSlaves: Int = Settings.defMaxSlaves) extends Actor with ActorLogging {
  import com._

  /* Stacks */

  /** List of target search links */
  private var targets = List[Search]()
  /** Set of all found links (to explore or explored) */
  private var foundLinks = HashSet[String]()
  /** List of all results found */
  private var results = List[Result]()
  /* Pool of slaves */
  private var slaves = ((0 to maxSlaves) map (_ =>
    asys.actorOf(Props(new Slave)))
  ).toSet
  /** Total number of missed URLs */
  private var nbMissed = 0

  /** Newly found links, not yet commited to the main hash set*/
  // TODO: ideally, such design should not be necessary
  private var newFoundLinks = HashSet[String]()
  // List of unexplored targets */
  private var newTargets = List[Search]()

  /* Main */

  def receive = {
    /* Launching a new search */
    case srch @ Search(url, query, _) =>
      this.foundLinks += url.link // Adding the base url string
      val slave = slaves.head // Acting via first slave
      this.slaves -= slave
      slave ! srch

    /* New result received form a slave */
    case res @ Result(srch @ Search(url, query, _), matches, links) =>
      val mw = new ModeWrapper(query.mode); import mw._ // Get search mode
      this.slaves += sender // Adding back sender as idle
      this.newTargets = this.newTargets ::++ // Adding back non existing urls
        ((links filter (l => !this.newFoundLinks.contains(l))).toList
          map (l => Search(Url(l, url.depth + 1), query)))
      this.newFoundLinks ++= links // Adding links back to the hashSet
      if (res.isPositive) results :+= res // Adding result

      // If current target list is empty, moving the newly added targets back
      // to the main list
      if (this.targets.size == 0) {
        this.targets = this.newTargets.filter(t =>
          !this.foundLinks.contains(t.url.link)) // Ensuring no duplicates
        this.foundLinks ++= this.newFoundLinks
        this.newFoundLinks = HashSet[String]() // Resetting
        this.newTargets = List[Search]() // Resetting
      }
      this.startRound // Start a new search round

    /* If a search failed, checking number of tentatives and re-scheduling */
    case Failed(search) =>
      val mw = new ModeWrapper(search.query.mode); import mw._
      this.slaves += sender
      if (search.coTentatives < Settings.defMaxCoTentatives) {
        this.targets = this.targets ::+
          search.copy(coTentatives = search.coTentatives + 1)
      } else nbMissed += 1
      this.startRound // Start a new search round

    /* UI-related commands */

    case AskProgress => sender ! AswProgress(this.progress, this.nbExplored,
      this.foundLinks.size + this.newFoundLinks.size, this.results.size,
      this.nbMissed)
    case AskResults =>  sender ! AswResults(this.results)
  }

  /* Helper */

    /** Simply starting a new seach round based on current state. */
    private def startRound = if (!this.targets.isEmpty) {
      val tpls = (this.slaves zip this.targets) // Scheduling all idle slaves
      this.slaves = this.slaves.drop(tpls.size)
      this.targets = targets.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }

    /** @return Count of explored links */
    private def nbExplored = this.foundLinks.size +
      this.newFoundLinks.size - this.targets.size - this.newTargets.size
    /** @return current progress knowledge */
    private def progress = this.nbExplored.toDouble / (
      this.foundLinks.size.toDouble + this.newFoundLinks.size.toDouble
    )
}
