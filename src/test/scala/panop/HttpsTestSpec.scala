import org.scalatest._
import scalaj.http._

class HttpsTestSpec extends FlatSpec {
  "A HTTP page" should "be loaded without problem" in {
    val res = Http("http://www.lemonde.fr/").asString
    assert(res.body contains "Monde")
  }
  "A HTTPS page" should "be loaded without problem" in {
    val res = Http("https://www.google.ch/").asString
    println(res.body)
    assert(res.body contains "Google")
  }
}