package controllers

import java.util.concurrent.Future
import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

@Singleton
class TitleFinderController @Inject()(cc: ControllerComponents, ws: WSClient ) extends AbstractController(cc) {

  def findTitle(url: String) = Action.async {
    ws.url(url).withRequestTimeout(5000.millis).get().map { response =>
      val html = response.body
      val startIndex = html.indexOf("<title>") + 7
      val endIndex = html.indexOf("</title>")
      val title = html.substring(startIndex, endIndex)
      println(title)
      Ok(title)
    }
  }

}
