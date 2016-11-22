package panop
package com

import scala.util.Random

/**
 * Define the type of behavior used to explore urls.
 * Can be in Breath First Search, Depth First Search, or Random
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
trait Mode
case object BFSMode extends Mode // Breath-first-search mode
case object DFSMode extends Mode // Depth-first-search mode
case object RNDMode extends Mode // Random mode

/** Defines the logic associated with each Mode. */
class ModeWrapper(mode: Mode) {
  implicit class RichUrls(ths: List[Search]) {
    /** Randomizes a list of search to be executed. */
    private def randomize(lst: List[Search]): List[Search] = {
      val rnd = new Random
      ((lst map (el => (el, rnd.nextInt))) sortBy (_._2)) map (_._1)
    }
    /** Add a lsit of search in a specific mode. */
    def ::++(tha: List[Search]): List[Search] = mode match {
      case BFSMode => ths ::: tha
      case DFSMode => tha ::: ths
      case RNDMode => randomize(ths ::: tha)
    }
    /** Add one specific search in a specific mode. */
    def ::+(tha: Search): List[Search] = mode match {
      case BFSMode => ths :+ tha
      case DFSMode => tha :: ths
      case RNDMode => randomize(ths :+ tha)
    }
  }
}
