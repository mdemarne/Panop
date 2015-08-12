package panop
package com

import scala.util.matching.Regex

/**
 * Query bean.
 * Poss and Negs are in disjunctive normal form.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
case class Query(
  poss: Seq[Seq[String]],
  negs: Seq[Seq[String]],                                                     
  maxDepth: Int,
  linkPrefix: Option[String],
  mode: Mode = BFSMode,
  ignoredFileExtensions: Regex = "js|css|pdf|png|jpg|gif|jpeg|svg|tiff".r,
  boundaries: (Regex, Regex) = ("<body>|<BODY>".r, "</body>|</BODY>".r)
) {

  override def toString = " + (" + Query.printNormalForm(poss) + ") - (" + Query.printNormalForm(negs) + ")"

  def matches(content: String): Seq[Seq[String]] = {
    val htmlPattern = "<[^>]+>".r
    val rawText = htmlPattern.replaceAllIn(content, "")
    if (negs.isEmpty || !negs.exists(_.forall(rawText.contains(_)))) poss.filter(_.forall(rawText.contains(_)))
    else Nil
  }
}

object Query {
  def apply(pos: Seq[String], maxDepth: Int): Query = Query(pos :: Nil, Nil, maxDepth, None)
  def apply(w: String, maxDepth: Int): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, None)

  def apply(pos: Seq[String], maxDepth: Int, linkPrefix: String): Query = Query(pos :: Nil, Nil, maxDepth, Some(linkPrefix))
  def apply(w: String, maxDepth: Int, linkPrefix: String): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, Some(linkPrefix))

  def printNormalForm(nls: Seq[Seq[String]]) = nls.map(_.map(_.toString).mkString("(", " AND ", ")")).mkString(" OR ")
}
