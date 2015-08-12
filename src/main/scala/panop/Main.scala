package panop

import akka.actor._

/**
 * Launch and manage searches (main loop)
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Main {
  import com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")

  def main(args: Array[String]) = {
    // TODO: this is test bullshit
    val master = asys.actorOf(Props(new Master(asys, 1)))
    // Niveau, Geographique, OFS, definition des agglomérations
    /*val query = Query(
      ("niveau géographique" :: Nil) :: 
      ("OFS " :: Nil) ::
      ("OFS." :: Nil) ::
      ("OFS)" :: Nil) ::
      ("OFS," :: Nil) ::
      ("définition des agglomérations" :: Nil) ::
      ("Niveau géographique" :: Nil) :: 
      ("Définition des agglomérations" :: Nil) ::
      Nil, Nil, 10, Some("https://www.admin.ch/opc/fr/classified-compilation"))*/
    val query = Query(("Alphabet" :: "Google" :: Nil) :: Nil, Nil, 10000, None)
    println(query)
    //master ! Search(Url("https://news.google.com/"), query)
    //master ! Search(Url("https://www.admin.ch/opc/fr/classified-compilation/national.html"), query)
    //master ! Search(Url("http://www.lemonde.fr"), Query(("Suisse" :: Nil) :: ("suisse" :: Nil) :: Nil, Nil, 10, Some("http://www.lemonde.fr")))
    master ! Search(Url("http://localhost:9000/"), Query(("CrossStream" :: "Hebdo" :: Nil) :: ("wall" :: Nil) :: ("Please" :: Nil) :: Nil, Nil, 10, Some("http://localhost:9000/"), RNDMode))
    while (true) {
      Thread.sleep(10000)
      master ! DisplayResults
    }
  }

  def help = {
    println("""
      |PANOP
      |
      |NAME
      |\t panop
      |
      |SYNOPSIS
      |\t panop TODO
      |
      |DESCRIPTION
      |\t Simple Tool For Parallel Online Search - refer to https://github.com/mdemarne/Panop
      |
      |OPTIONS
      |\t TODO
    """.stripMargin)
  }
}