package panop

import akka.actor._

import scala.collection.immutable.HashSet

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Master controller for Panop search run.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
class Master(asys: ActorSystem, var maxSlaves: Int = Settings.defMaxSlaves) extends Actor with ActorLogging {
  import com._

  /* Stacks */

  private var targets = List[Search]()
  private var foundLinks = HashSet[String]()
  private var results = List[Result]()
  private var slaves = ((0 to maxSlaves) map (_ => asys.actorOf(Props(new Slave)))).toSet
  private var nbMissed = 0

  private var newFoundLinks = HashSet[String]()
  private var newTargets = List[Search]()

  /* Main */

  def receive = {
    case srch @ Search(url, query, _) =>
      this.foundLinks += url.link
      val slave = slaves.head
      this.slaves -= slave
      slave ! srch

    case res @ Result(srch @ Search(url, query, coTentatives), matches, links) =>
      val mw = new ModeWrapper(query.mode); import mw._
      this.slaves += sender
      this.newTargets = this.newTargets ::++ ((links filter (l => !this.newFoundLinks.contains(l))).toList map (l => Search(Url(l, url.depth + 1), query)))
      this.newFoundLinks ++= links
      if (res.isPositive) results :+= res
      if (this.targets.size == 0) {
        this.targets = this.newTargets.filter(t => !this.foundLinks.contains(t.url.link))
        this.foundLinks ++= this.newFoundLinks
        this.newFoundLinks = HashSet[String]()
        this.newTargets = List[Search]()
      }
      this.startRound

    case Failed(search) =>
      val mw = new ModeWrapper(search.query.mode); import mw._
      this.slaves += sender
      if (search.coTentatives < Settings.defMaxCoTentatives) this.targets = this.targets ::+ search.copy(coTentatives = search.coTentatives + 1)
      else nbMissed += 1
      this.startRound

    case AskProgress => sender ! AswProgress(this.progress, this.nbExplored, this.foundLinks.size + this.newFoundLinks.size, this.results.size, this.nbMissed)
    case AskResults =>  sender ! AswResults(this.results)
  }

  /* Helper */

    private def startRound = if (!this.targets.isEmpty) {
      val tpls = (this.slaves zip this.targets)
      this.slaves = this.slaves.drop(tpls.size)
      this.targets = targets.drop(tpls.size)
      tpls foreach (tpl => tpl._1 ! tpl._2)
    }

    private def nbExplored = this.foundLinks.size + this.newFoundLinks.size - this.targets.size - this.newTargets.size
    private def progress = this.nbExplored.toDouble / (this.foundLinks.size.toDouble + this.newFoundLinks.size.toDouble)
}