package controllers


import java.net.{ConnectException, UnknownHostException}
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}
import javax.net.ssl.SSLException

import play.api.data.Form
import play.api.http.HttpEntity
import play.api.libs.json.Json

import scala.concurrent.duration._
import play.api.libs.ws.WSClient
import play.api.mvc.{AbstractController, ControllerComponents, ResponseHeader, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

case class TitleResponse(title: String, errorMessage: String, valid: Boolean)

@Singleton
class TitleFinderController @Inject()(cc: ControllerComponents, ws: WSClient ) extends AbstractController(cc) {

  private val form: Form[TitleResponse] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "errorMessage" -> text,
        "valid" -> boolean
      )(TitleResponse.apply)(TitleResponse.unapply)
    )
  }

  implicit val titleWriter = Json.writes[TitleResponse]

  def findTitle(url: String) =  Action.async {
    Try(ws.url(url).withRequestTimeout(5000.millis).get().map { response =>
      val html = response.body.toLowerCase
      val startIndex = html.indexOf("<title>")
      val endIndex = html.indexOf("</title>")
      println(html)
      if ( startIndex != -1 ) {
        val title = html.substring(startIndex + 7, endIndex)
        val response: TitleResponse = new TitleResponse(title, "", true)
        Ok(Json.toJson(response))
      } else {
        val response: TitleResponse = new TitleResponse("",
          "Response did not have html, please enter a site that serves an html page.", false)
        Ok(Json.toJson(response))
      }
    }.recover {
      case t: TimeoutException => InternalServerError(Json.toJson(new TitleResponse("", "Request to host timed out. Host is busy or does not exist.", false)))
      case uk: UnknownHostException =>  InternalServerError(Json.toJson(new TitleResponse("", "The website you are trying to retrieve from does not exist or is not known.", false)))
      case ssl: SSLException => InternalServerError(Json.toJson(new TitleResponse("", "SSL Exception - try changing the protocol from https or http.", false)))
      case conn: ConnectException => InternalServerError(Json.toJson(new TitleResponse("", "SSL Exception - try changing the protocol from https or http. If that doesn't work, the connection was refused by host.", false)))
    })
      .getOrElse(Future.successful[Result](new Result(new ResponseHeader(512), HttpEntity.NoEntity)))

  }
}
