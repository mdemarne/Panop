import org.scalatest._
import scalaj.http._
import panop.com._

class QuerySpec extends FlatSpec {
  "A Query" should "match 01" in {
    val content = "aa"
    val poss: Seq[Seq[String]] = ("aa" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = Nil
    val query = Query(poss, negs, 0, None)
    assert(!query.matches(content).isEmpty)
  }
  it should "match 02" in {
    val content = "aa, bb"
    val poss: Seq[Seq[String]] = ("aa" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = Nil
    val query = Query(poss, negs, 0, None)
    assert(!query.matches(content).isEmpty)
  }
  it should "not match 03" in {
    val content = "aa, bb.cc"
    val poss: Seq[Seq[String]] = ("aa" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = ("cc" :: Nil) :: Nil
    val query = Query(poss, negs, 0, None)
    assert(query.matches(content).isEmpty)
  }
  it should "match 04" in {
    val content = "aa, bb, cc"
    val poss: Seq[Seq[String]] = ("aa" :: "bb" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = Nil
    val query = Query(poss, negs, 0, None)
    assert(!query.matches(content).isEmpty)
  }
  it should "match 05" in {
    val content = "aa, bb"
    val poss: Seq[Seq[String]] = ("aa" :: Nil) :: ("cc" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = Nil
    val query = Query(poss, negs, 0, None)
    assert(!query.matches(content).isEmpty)
  }
  it should "remove html tags" in {
    val content = "<body>aa, bb</body>"
    val poss: Seq[Seq[String]] = ("aa" :: Nil) :: ("cc" :: Nil) :: Nil
    val negs: Seq[Seq[String]] = Nil
    val query = Query(poss, negs, 0, None)
    assert(!query.matches(content).isEmpty)
  }
}