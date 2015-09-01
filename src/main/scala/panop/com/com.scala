package panop

/**
 * Contains a few communication case classes and object.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
package object com {

  case class Url(link: String, depth: Int) {
    override def toString = link.toString
  }
  object Url {
    def apply(link: String): Url = Url(link, 0)
  }

  case class Search(url: Url, query: Query, coTentatives: Int = 0) {
    override def toString = s"$query\nOn: $url"
  }
  case class Result(search: Search, matches: Seq[Seq[String]], links: Set[String]) {
    def isPositive = !matches.isEmpty
  }
  case class Failed(search: Search)

  case object AskResults
  case class AswResults(results: List[Result])

  case object AskProgress
  case class AswProgress(percent: Double, nbExplored: Int, nbFound: Int, nbMatches: Int, nbMissed: Int)
}