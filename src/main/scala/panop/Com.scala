package panop

/**
 * Contains a few communication means
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
object Com {

  /* Pos and Neg are in disjunctive normal form */
  case class Query(pos: Seq[Seq[String]], neg: Seq[Seq[String]]) {
    override def toString = ??? // TODO
  }

  case class Url(link: String, depth: Int) {
    override def toString = link.toString
  }

  case class Search(url: Url, query: Query)
  case class Result(search: Search, isPositive: Boolean, links: List[String])

  case class StartSearch(url: Url, query: Query, depth: Int)
  case object DisplayResults
}