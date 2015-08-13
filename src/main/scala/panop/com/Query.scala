package panop
package com

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
 * Query bean.
 * Poss and Negs are in disjunctive normal form.
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
case class Query(
  poss: Seq[Seq[String]],
  negs: Seq[Seq[String]],                                                     
  maxDepth: Int,
  domain: Option[String],
  mode: Mode,
  ignoredFileExtensions: Regex,
  boundaries: (Regex, Regex)
) {

  def matches(content: String): Seq[Seq[String]] = {
    val htmlPattern = "<[^>]+>".r
    val rawText = htmlPattern.replaceAllIn(content, "")
    if (negs.isEmpty || !negs.exists(_.forall(rawText.contains(_)))) poss.filter(_.forall(rawText.contains(_)))
    else Nil
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

object Query {

  val defMode = BFSMode
  val defIgnExts = "js|css|pdf|png|jpg|gif|jpeg|svg|tiff".r
  val defTopBnds = "<body>|<BODY>".r
  val defBotBnds = "</body>|</body>".r
  val defMaxSlaves = 200

  def apply(pos: Seq[String], maxDepth: Int): Query = Query(pos :: Nil, Nil, maxDepth, None, defMode, defIgnExts, (defTopBnds, defBotBnds))
  def apply(w: String, maxDepth: Int): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, None, defMode, defIgnExts, (defTopBnds, defBotBnds))
  
  def apply(pos: Seq[String], maxDepth: Int, domain: String): Query = Query(pos :: Nil, Nil, maxDepth, Some(domain), defMode, defIgnExts, (defTopBnds, defBotBnds))
  def apply(w: String, maxDepth: Int, domain: String): Query = Query((w :: Nil) :: Nil, Nil, maxDepth, Some(domain), defMode, defIgnExts, (defTopBnds, defBotBnds))

  def apply(poss: Seq[Seq[String]], negs: Seq[Seq[String]], maxDepth: Int): Query = Query(poss, negs, maxDepth, None, defMode, defIgnExts, (defTopBnds, defBotBnds))
  def apply(poss: Seq[Seq[String]], negs: Seq[Seq[String]], maxDepth: Int, domain: String): Query = Query(poss, negs, maxDepth, Some(domain), defMode, defIgnExts, (defTopBnds, defBotBnds))

  def printNormalForm(nls: Seq[Seq[String]]) = nls.map(_.map(_.toString).mkString("(", " AND ", ")")).mkString(" OR ")
  def printLogicalQuery(poss: Seq[Seq[String]], negs: Seq[Seq[String]]) = Query.printNormalForm(poss) + " - " + Query.printNormalForm(negs)
}

object QueryParser extends RegexParsers {
  private def word: Parser[String] = "'[^']+'".r ^^ { case e => e.tail.init }
  private def conj: Parser[Seq[String]] = (
    "(" ~> word ~ ("AND" ~> word).* <~ ")" ^^ {
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
  def apply(str: String): Either[(Seq[Seq[String]], Seq[Seq[String]]), String] = {
    parseAll(tupl, str) match {
      case Success(t, _) => Left(t)
      case Error(e, r) => Right(e)
      case Failure(e, r) => Right(e)
    }
  }
}
