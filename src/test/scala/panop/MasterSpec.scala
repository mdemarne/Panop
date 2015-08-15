import org.scalatest._
import scalaj.http._
import panop._
import akka.actor._

class MasterSpec extends FlatSpec {
  import panop.com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")

  "A master" should "start a very simple search of depth 0" in {
    val master = asys.actorOf(Props(new Master(asys)))
    master ! Search(Url("https://news.google.com"), Query("Obama" :: Nil, 0))
    Thread.sleep(1000)
    master ! DisplayResults
    // TODO: proper testing
  }
  "A master" should "start a very simple search of depth 1" in {
    val master = asys.actorOf(Props(new Master(asys)))
    master ! Search(Url("https://news.google.com"), Query("Obama" :: Nil, 1))
    Thread.sleep(10000)
    master ! DisplayResults
    // TODO: proper testing
  }
}