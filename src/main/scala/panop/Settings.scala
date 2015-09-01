package panop

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
 * Setting object containig default parameters.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Settings {
  import com._
  // TODO: in the future, this could be moved to Akka configuration for instance.
  val defMaxDepth = 20
  val defDepth = 5
  val defMode = BFSMode
  val defIgnExts = "js|css|pdf|png|jpg|gif|jpeg|svg|tiff".r
  val defTopBnds = "<body>|<BODY>".r
  val defBotBnds = "</body>|</BODY>".r
  val defMaxSlaves = 30
  val defSlaves = 10
  val defMaxCoTentatives = 10
}