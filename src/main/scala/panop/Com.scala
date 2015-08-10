package panop

/**
 * Contains a few communication means
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Com {

  case class Url(link: String, depth: Int) {
    override def toString = link.toString
  }
  object Url {
    def apply(link: String): Url = Url(link, 0)
  }

  case class Search(url: Url, query: Query)
  case class Result(search: Search, isPositive: Boolean, links: List[String])
  case class Failed(search: Search)

  case class StartSearch(url: Url, query: Query)
  case object DisplayResults
}