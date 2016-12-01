import org.scalatest._
import scalaj.http._
import panop._
import akka.actor._

/** Check the Master Actor. */
class MasterSpec extends FlatSpec {
  import panop.com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")

  "A master" should "start a very simple search of depth 0" in {
    val master = asys.actorOf(Props(new Master(asys)))
    master ! Search(Url("https://www.google.ch"), Query("Google" :: Nil, 0))
    Thread.sleep(10000)
    master !? AskResults match {
      case AswResults(results) => assert(results.size == 1)
      case _ => fail
    }
  }
  it should "start a very simple search of depth 1" in {
    val master = asys.actorOf(Props(new Master(asys)))
    master ! Search(Url("https://www.google.ch"), Query("Google" :: Nil, 1))
    Thread.sleep(10000)
    master !? AskResults match {
      case AswResults(results) => assert(results.size > 1)
      case _ => fail
    }
  }
}
