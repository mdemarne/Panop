package panop

/**
 * Contains a few communication means
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Com {

  case class Url(link: String, depth: Int) {
    override def toString = link.toString
  }

  case class Search(url: Url, query: Query, maxDepth: Int)
  case class Result(search: Search, isPositive: Boolean, links: List[String])

  case class StartSearch(url: Url, query: Query, depth: Int)
  case object DisplayResults
}