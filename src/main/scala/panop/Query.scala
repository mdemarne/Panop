package panop

import scala.util.matching.Regex

/**
 * Query mean.
 * Pos and Neg are in disjunctive normal form
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
case class Query(
  poss: Seq[Seq[String]], 
  negs: Seq[Seq[String]], 
  maxDepth: Int,
  ignoredFileExtensions: Seq[String] = ".js" :: ".css" :: Nil,  // TODO: remove hard coded
  boundaries: (Regex, Regex) = ("<body>|<BODY>".r, "</body>|<BODY>".r)) { // TODO: idem

  private def printNormalForm(nls: Seq[Seq[String]]) = nls.map(_.map(_.toString).mkString(" ^ ")).mkString(" ∩ ")
  override def toString = " + (" + printNormalForm(poss) + ") - (" +printNormalForm(negs) + ")"
  def matches(content: String) = {
    val htmlPattern = "<[^>]+>".r
    val rawText = htmlPattern.replaceAllIn(content, " ")
    val tokens = rawText.split(" |,|\\.|\n|\t").filter(_.size > 1).toSet // TODO: remove stop words, etc. Here removing only one-char tokens
    poss.exists(_.forall(tokens.contains(_))) && (!negs.forall(_.forall(tokens.contains(_))) || negs.isEmpty)
  }
}

object Query {
  def apply(pos: Seq[String], maxDepth: Int): Query = Query(pos :: Nil, Nil, maxDepth)
  def apply(w: String, maxDepth: Int): Query = Query((w :: Nil) :: Nil, Nil, maxDepth)
}
