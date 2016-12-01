package panop.ui

import panop._

import akka.actor._
import scala.util.{ Try, Success, Failure }
import scala.util.matching.Regex
import scala.annotation.tailrec

import java.util.Scanner


/**
 * Launch and manage searches (main loop)
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Main {
  import com._
  import Enrichments._

  def main(args: Array[String]) = {
    /* Get all arguments */
    args.toList match {
      /* If it is help */
      case opts if opts.contains("--help") => help

      /* Otherwise: assume query string, then url, then options */
      case queryStr :: url :: opts =>

        /** Gets argument for a specific key. Assume only one entry per key. */
        def filterOpts(key: String) = opts.filter(_.startsWith(key))
          .map(_.drop(key.length))

        /* Parses query. */
        val query: (Seq[Seq[String]], Seq[Seq[String]]) =
          QueryParser(queryStr) match {
            case Left(qr) => qr
            case Right(err) => fatal(err)
          }
        /* Get max depth argument. */
        val maxDepth: Int = filterOpts("--max-depth=") match {
          case Nil => Settings.defDepth
          case x :: Nil => Try(x.toInt) match {
            case Success(max) if max > 0 => Math.min(max, Settings.defMaxDepth)
            case Failure(_) => fatal("--max-depth must be a positive integer")
          }
          case _ => fatal("Cannot specify more than once the maximum depth!")
        }

        /* Get domain */
        val domain: Option[String] = filterOpts("--domain=") match {
          case Nil => None
          case x :: Nil => Some(x)
          case _ => fatal("Cannot specify more than one domain!")
        }

        /* Select mode */
        val mode: Mode = filterOpts("--mode=") match {
          case Nil => Settings.defMode
          case x :: Nil if x == "BFS" => BFSMode
          case x :: Nil if x == "DFS" => DFSMode
          case x :: Nil if x == "RND" => RNDMode
          case x :: Nil => fatal("Wrong mode inserted")
          case _ => fatal("Cannot support more than one mode!")
        }

        /* Get regex for ignored files */
        val ignExts: Regex = filterOpts("--ignored-exts=") match {
          case Nil => Settings.defIgnExts
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for ignored ext.!")
        }

        /* Get Boundaries (top, bottom) */
        val topBnds: Regex = filterOpts("--boundaries-top=") match {
          case Nil => Settings.defTopBnds
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for top boundaries!")
        }
        val botBnds: Regex = filterOpts("--boundaries-bottom=") match {
          case Nil => Settings.defBotBnds
          case x :: Nil => x.r
          case _ => fatal("Cannot support multiple regex for top boundaries!")
        }

        /* Get the minimum number of slaves requires */
        val MaxSlaves: Int = filterOpts("--max-slaves=") match {
          case Nil => Settings.defSlaves
          case x :: Nil => Try(x.toInt) match {
            case Success(max) if max > 0 => Math.min(max, Settings.defMaxSlaves)
            case Failure(_) => fatal("--max-slave must be a positive integer.")
          }
          case _ => fatal("Cannot specify more than once the slave number!")
        }

        /* Generate the actor system, create the master node */
        val asys = ActorSystem.create("Panop")
        val master = asys.actorOf(Props(new Master(asys, MaxSlaves)))
        val search = Search(Url(url), Query(query._1, query._2, maxDepth,
          domain, mode, ignExts, (topBnds, botBnds)))
        /* Start executing! */
        exec(asys, master, search)
      case _ => fatal("Error loading parameters. 'panop --help' for usage.")
    }
  }

  /** control loop: once an actor system specified and a master created. Loop
   *  on user arguments. */
  private def exec(asys: ActorSystem, master: ActorRef, search: Search) = {
    val sc = new Scanner(System.in)
    @tailrec def loop: Unit = {
      sc.nextLine match {
        /* Display current progress status */
        case "progress" =>
          master !? AskProgress match {
            case AswProgress(progress, nbExplored, nbFound, nbMatches,
              nbMissed) =>
              println("---------------------------------------------")
              println(s"Progress: $progress (explored $nbExplored over " +
                "$nbFound links).")
              println(s"Found $nbMatches matches. $nbMissed urls could not " +
                "be explored.")
              println("---------------------------------------------")
            case _ => fatal("Wrong result type for AskProgress.")
          }
          loop /* Loop again! */

        /* Display current result status */
        case "results" =>
          master !? AskResults match {
            case AswResults(results) =>
              println("---------------------------------------------")
              println("Displaying results...")
              results.sortBy(r =>
                Query.printNormalForm(r.matches)
              ) foreach (r =>
                println("\t" + r.search.url.link +
                  " [" + Query.printNormalForm(r.matches) + "]")
              )
              println("---------------------------------------------")
            case _ => fatal("Wrong result type for AskResults.")
          }
          loop

        /* Display help */
        case "help" =>
          help
          loop

        /* Shutdown the actor system and stop any search */
        case "exit" =>
          println("Stopping all searches..")
          asys.shutdown

        /* Anything else is wrong. */
        case _ =>
          println("Wrong command")
          loop
      }
    }

    /* Startup execution: starts the loop. */
    master ! search // Start the search
    println(s"$search")
    println("Enter 'progress', 'results', 'help' or 'exit' at any time.")
    loop
  }

  private def fatal(mess: String) = {
    sys.error(mess)
    sys.exit(-1)
  }

  private def help = { // TODO: line return
    println("""
      |PANOP
      |
      |NAME
      |  panop
      |
      |SYNOPSIS
      |  panop [QUERY] [URL] [OPTIONS]
      |
      |DESCRIPTION
      |  Simple Tool For Parallel Online Search - refer to
      |  https://github.com/mdemarne/panop-core
      |
      |OPTIONS
      |  --help
      |    Display this help.
      |  --max-depth=NUMBER
      |    Maximum depth recursion for the research tree (by default, 5)
      |  --domain=URLPREFIX
      |    Specify the url prefix for all explored link. By default, the search
      |    will expand beyond the original domain.
      |  --mode=MODE
      |    Apply various lookup mode for found URLs (BFS, DFS, RND (random))
      |    (by default, BFS).
      |  --ignored-exts=REGEX
      |    Regex for ignored extensions (Default for all images, PDF, SVG,
      | CSS, Javascript, etc.).
      |  --boundaries-top=REGEX
      |    Top boundary in which a code will be considered.
      |  --boundaries-bottom=REGEX
      |    Bottom boundary in which a code will be considered.
      |  --max-slaves=NUMBER
      |    Number of slaves to use to execute parallel searching.
      |
      |COMMANDS
      |  progress
      |    Display progress.
      |  results
      |    Display results, full or temporary.
      |  exit
      |    Stop any running search.
    """.stripMargin)
  }
}
