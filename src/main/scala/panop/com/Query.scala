package panop
package com

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
 * Query object.
 * Poss and Negs are in disjunctive normal form.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 * @todo   This could use a specifc, simple DSL.
 * @todo   This is HTML-specific. patterns to ignore should be made a property
 *         of the system if Panop is to be ported on non-html content
 *         (e.g. we may want it to behave differently on XML).
 */
case class Query(
  poss: Seq[Seq[String]],        // Positive search terms
  negs: Seq[Seq[String]],        // Negative searc terms
  maxDepth: Int,                 // Maximum depth allowed for this search.
  domain: Option[String],        // Optional domain boundaries
  mode: Mode,                    // Search mode
  ignoredFileExtensions: Regex,  // Signored file regex
  boundaries: (Regex, Regex)     // boundaries regex (start, end).
                                 // First match are considered only.
) {

  /** Find all matches in a given content.*/
  def matches(content: String): Seq[Seq[String]] = {
    // Cleanup HTML code: we are not interest of what HTML balises contains.
    val htmlPattern = "<[^>]+>".r
    val rawText = htmlPattern.replaceAllIn(content, "")
    if (negs.isEmpty || !negs.exists(_.forall(rawText.contains(_)))) {
      poss.filter(_.forall(rawText.contains(_)))
    } else Nil
  }

  override def toString = {
    s"""
      |Query: ${Query.printLogicalQuery(poss, negs)}
      |Max Depth: $maxDepth
      |Domain: ${domain.getOrElse("none")}
      |Mode: $mode
      |Ignored file extensions: $ignoredFileExtensions
      |Boundaries:
      |  ${boundaries._1}
      |  ...
      |  ${boundaries._2}
    """.stripMargin
  }
}

/** Contains default builders */
object Query {

  def apply(pos: Seq[String], maxDepth: Int): Query =
    Query(pos :: Nil, Nil, maxDepth, None, Settings.defMode,
      Settings.defIgnExts, (Settings.defTopBnds, Settings.defBotBnds))

  def apply(w: String, maxDepth: Int): Query =
    Query((w :: Nil) :: Nil, Nil, maxDepth, None, Settings.defMode,
      Settings.defIgnExts, (Settings.defTopBnds, Settings.defBotBnds))

  def apply(pos: Seq[String], maxDepth: Int, domain: String): Query =
    Query(pos :: Nil, Nil, maxDepth, Some(domain), Settings.defMode,
      Settings.defIgnExts, (Settings.defTopBnds, Settings.defBotBnds))

  def apply(w: String, maxDepth: Int, domain: String): Query =
    Query((w :: Nil) :: Nil, Nil, maxDepth, Some(domain), Settings.defMode,
      Settings.defIgnExts, (Settings.defTopBnds, Settings.defBotBnds))

  def apply(poss: Seq[Seq[String]], negs: Seq[Seq[String]],
    maxDepth: Int): Query =
    Query(poss, negs, maxDepth, None, Settings.defMode, Settings.defIgnExts,
      (Settings.defTopBnds, Settings.defBotBnds))
  def apply(poss: Seq[Seq[String]], negs: Seq[Seq[String]], maxDepth: Int,
    domain: String): Query =
    Query(poss, negs, maxDepth, Some(domain), Settings.defMode,
      Settings.defIgnExts, (Settings.defTopBnds, Settings.defBotBnds))

  def printNormalForm(nls: Seq[Seq[String]]) =
    nls.map(_.map(_.toString).mkString("(", " AND ", ")")).mkString(" OR ")
  def printLogicalQuery(poss: Seq[Seq[String]], negs: Seq[Seq[String]]) =
    Query.printNormalForm(poss) + " - " + Query.printNormalForm(negs)
}

/** Simple Query parser */
object QueryParser extends RegexParsers {
  private def word: Parser[String] = "'[^']+'".r ^^ { case e => e.tail.init }
  private def conj: Parser[Seq[String]] = (
    "\\(?".r ~> word ~ ("AND" ~> word).* <~ "\\)?".r ^^ {
      case e ~ Nil => Seq(e)
      case e ~ e1 => Seq(e) ++ e1.toSeq
    }
  )
  private def disj: Parser[Seq[Seq[String]]] = (
    conj ~ ("OR" ~> conj).* ^^ {
      case e ~ Nil => Seq(e)
      case e ~ e1 => Seq(e) ++ e1.toSeq
    }
  )
  private def tupl: Parser[(Seq[Seq[String]], Seq[Seq[String]])] = (
    disj ~ opt(("-" ~> disj)) ^^ {
      case poss ~ Some(negs) => (poss, negs)
      case poss ~ None => (poss, Seq())
    }
  )
  def apply(str: String):
    Either[(Seq[Seq[String]], Seq[Seq[String]]), String] = {
    parseAll(tupl, str) match {
      case Success(t, _) => Left(t)
      case Error(e, r) => Right(e)
      case Failure(e, r) => Right(e)
    }
  }
}
