package panop
package com

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
  linkPrefix: Option[String],
  ignoredFileExtensions: Regex = "js|css|pdf|png|jpg|gif|jpeg|svg|tiff".r, // TODO: remove hard coded
  boundaries: (Regex, Regex) = ("<body>|<BODY>".r, "</body>|</BODY>".r)) { // TODO: idem

  private def printNormalForm(nls: Seq[Seq[String]]) = nls.map(_.map(_.toString).mkString("(", " AND ", ")")).mkString(" OR ")
  override def toString = " + (" + printNormalForm(poss) + ") - (" + printNormalForm(negs) + ")"
  def matches(content: String) = {
    val htmlPattern = "<[^>]+>".r
    val rawText = htmlPattern.replaceAllIn(content, " ")
    poss.exists(_.forall(rawText.contains(_))) && (!negs.forall(_.forall(rawText.contains(_))) || negs.isEmpty)
  }
}

object Query {
  def apply(pos: Seq[String], maxDepth: Int): Query = Query(pos :: Nil, Nil, maxDepth, None)
  def apply(w: String, maxDepth: Int): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, None)

  def apply(pos: Seq[String], maxDepth: Int, linkPrefix: String): Query = Query(pos :: Nil, Nil, maxDepth, Some(linkPrefix))
  def apply(w: String, maxDepth: Int, linkPrefix: String): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, Some(linkPrefix))
}
