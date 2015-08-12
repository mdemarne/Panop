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

  "A QueryParser" should "do simple parsing" in {
    val queryStr = "('TEST 01')"
    assert(QueryParser.parse(queryStr) == Left(Seq(Seq("TEST 01"))))
  }
  it should "do simple conjunctions" in {
    val queryStr = "('TEST 01' AND 'TEST 02')"
    assert(QueryParser.parse(queryStr) == Left(Seq(Seq("TEST 01", "TEST 02"))))
  }
  it should "do simple disjunctions" in {
    val queryStr = "('TEST 01') OR ('TEST 02')"
    assert(QueryParser.parse(queryStr) == Left(Seq(Seq("TEST 01"), Seq("TEST 02"))))
  }
  it should "do more complex disjunctions" in {
    val queryStr = "('TEST 01' AND 'TEST 02') OR ('TEST 03') OR ('TEST 04' AND 'TEST 05' AND 'TEST 06')"
    assert(QueryParser.parse(queryStr) == Left(Seq(Seq("TEST 01", "TEST 02"), Seq("TEST 03"), Seq("TEST 04", "TEST 05", "TEST 06"))))
  }
}