package panop

/**
 * Contains a few communication case classes and object.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
package object com {

  /* QUERY MECHANISMS */

  /** Url encapsulation.
   *  @param link   The HTTP url itself
   *  @param depth  The current research depth. */
  case class Url(link: String, depth: Int = 0) {
    override def toString = link.toString
  }

  /** Represents a search to be made on a specific Url, for a specific query,
   *  @param url            The Url to search
   *  @param query          The query associated with the search
   *  @param coTentatives   The number of tentatives used to sar */
  case class Search(url: Url, query: Query, coTentatives: Int = 0) {
    override def toString = s"$query\nOn: $url"
  }

  /** Represent a result along with the associated base search.
   *  @param search     The original search
   *  @param matches    Sequence of tuples of matching string (organized as a
   *                    sequence, as tuples vary in length).
   *  @param links      Link found when walking the content associated with the
   *                    search. */
  case class Result(search: Search, matches: Seq[Seq[String]],
    links: Set[String]) {
    def isPositive = !matches.isEmpty
  }

  /** Represents a failed search. */
  case class Failed(search: Search)

  /* SEARCH WORKFLOW COMMUNICATION */

  /* Communication used by UI/Console to display results */
  case object AskResults
  case class AswResults(results: List[Result])

  /* Communication used by UI/Console to request progress stats */
  case object AskProgress
  case class AswProgress(percent: Double, nbExplored: Int, nbFound: Int,
    nbMatches: Int, nbMissed: Int)
}
