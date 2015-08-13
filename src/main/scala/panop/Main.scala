package panop

import akka.actor._
import scala.util.{Try, Success, Failure}
import scala.util.matching.Regex

/**
 * Launch and manage searches (main loop)
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Main {
  import com._
  import Enrichments._

  val asys = ActorSystem.create("SlaveSpecSys")

  def main(args: Array[String]) = {
    args.toList match { 
      case opts if opts.contains("--help") => help
      case queryStr :: url :: domain :: depthStr :: opts => 

        def filterOpts(key: String) = opts.filter(_.startsWith(key)).map(_.drop("key".length))

        val query: (Seq[Seq[String]], Seq[Seq[String]]) = QueryParser(queryStr) match {
          case Left(qr) => qr
          case Right(err) => fatal(err)
        }
        val depth = Try(depthStr.toInt) match {
          case Success(d) if d > 0 => d
          case _ => fatal("the depth should be a positive integer.")
        }
        val mode: Mode = filterOpts("--mode") match {
          case Nil => Query.defMode
          case x :: Nil if x == "BFS" => BFSMode
          case x :: Nil if x == "DFS" => DFSMode
          case x :: Nil if x == "RND" => RNDMode
          case x :: Nil => fatal("Wrong mode inserted")
          case _ => fatal("Cannot support more than one mode!")
        }
        val IgnExts: Regex = filterOpts("--ignored-ext") match {
          case Nil => Query.defIgnExts
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for ignored extensions!")
        }
        val topBnds: Regex = filterOpts("--boundaries-top") match {
          case Nil => Query.defTopBnds
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for top boundaries!")
        }
        val botBnds: Regex = filterOpts("--boundaries-bottom") match {
          case Nil => Query.defBotBnds
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for top boundaries!")
        }
        val MaxSlaves: Int = filterOpts("--max-slaves") match {
          case Nil => Query.defMaxSlaves
          case x :: Nil => Try(x.toInt) match {
          case Success(max) if max > 0 => max
          case Failure(_) => fatal("--max-slave must be a positive integer.")
          }
          case _ => fatal("Cannot specify more than once the maximum number of slaves!")
        }
      case _ => fatal("Error while loading parameters. Enter 'panop --help' for usage.")
    }
    /*// TODO: this is test bullshit
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
    }*/
  }

  private def loop(master: ActorRef) = ???

  private def fatal(mess: String) = sys.error(mess)

  private def help = {
    println("""
      |PANOP
      |
      |NAME
      |\t panop
      |
      |SYNOPSIS
      |\t panop [QUERY] [URL] [DOMAIN] [DEPTH] [OPTIONS]
      |
      |DESCRIPTION
      |\t Simple Tool For Parallel Online Search - refer to https://github.com/mdemarne/Panop
      |
      |OPTIONS
      |\t --help 
      |\t\t Display this help.
      |\t --mode=MODE
      |\t\t Apply various lookup mode for found URLs (BFS, DFS, RND (random)).
      |\t --ignored-ext=REGEX
      |\t\t Regex for ignored extensions (Default for all images, PDF, SVG, CSS, Javascript, etc.).
      |\t --boundaries-top=REGEX
      |\t\t Top boundary in which a code will be considered.
      |\t --boundaries-bottom=REGEX
      |\t\t Bottom boundary in which a code will be considered.
      |\t --max-slaves=NUMBER
      |\t\t Number of slaves to use to execute parallel searching.
    """.stripMargin)
  }
}