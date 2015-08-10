package panop

import akka.actor._

/**
 * Launch and manage research (main loop)
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Main {
  import Com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")

  def main(args: Array[String]) = {
    // TODO: this is test bullshit
    val master = asys.actorOf(Props(new Master(asys)))
    master ! StartSearch(Url("https://www.admin.ch/opc/fr/classified-compilation/national.html"), Query(("Peuple" :: Nil) :: Nil, Nil, 10, Some("https://www.admin.ch/opc/fr/classified-compilation")))
    while(true) {
      Thread.sleep(10000)
      master ! DisplayResults
    }
  }
}