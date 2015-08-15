package panop
package com

import scala.util.Random

/**
 * Define the type of behavior used to explore urls. 
 * Can be in Breath First Search, Depth First Search, or Random
 * @author Mathieu Demarne (mathieu.demarne@gmail.com)
 */
trait Mode
case object BFSMode extends Mode
case object DFSMode extends Mode
case object RNDMode extends Mode

class ModeWrapper(mode: Mode) {
  implicit class RichUrls(ths: List[Search]) {
    private def randomize(lst: List[Search]): List[Search] = {
      val rnd = new Random
      ((lst map (el => (el, rnd.nextInt))) sortBy (_._2)) map (_._1)
    }
    def ::++(tha: List[Search]): List[Search] = mode match {
      case BFSMode => ths ::: tha
      case DFSMode => tha ::: ths
      case RNDMode => randomize(ths ::: tha)
    }
    def ::+(tha: Search): List[Search] = mode match {
      case BFSMode => ths :+ tha
      case DFSMode => tha :: ths
      case RNDMode => randomize(ths :+ tha)
    }
  }
}