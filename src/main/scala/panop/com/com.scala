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

  case class Search(url: Url, query: Query) {
    override def toString = s"$query\nOn: $url"
  }
  case class Result(search: Search, matches: Seq[Seq[String]], links: Set[String]) {
    def isPositive = !matches.isEmpty
  }
  case class Failed(search: Search)
  case object DisplayResults
  case object DisplayProgress
}