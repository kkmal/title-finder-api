import controllers.TitleFinderController
import org.scalatestplus.play._
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers._

/**
  * Unit tests can run without a full Play application.
  */
class UnitSpec extends PlaySpec {

  "TitleFinderComponent" should {
    "return title as Google" in {
      val ws: WSClient = new  WSClient {override def underlying[T]: T = ???

        override def url(url: String): WSRequest = {
          return null
        }

        override def close(): Unit = ???
      }
      val controller = new TitleFinderController(stubControllerComponents(), ws)

      controller.findTitle("https://google.com")

    }
  }


}