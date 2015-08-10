package panop

/**
 * Query mean.
 * Pos and Neg are in disjunctive normal form
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
case class Query(poss: Seq[Seq[String]], negs: Seq[Seq[String]]) {
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
  def apply(pos: Seq[String]): Query = Query(pos :: Nil, Nil)
  def apply(w: String): Query = Query((w :: Nil) :: Nil, Nil)
}
