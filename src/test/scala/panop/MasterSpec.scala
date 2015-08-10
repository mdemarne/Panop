import org.scalatest._
import scalaj.http._
import panop._
import akka.actor._

class MasterSpec extends FlatSpec {
  import Com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")
  
}