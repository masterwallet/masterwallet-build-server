import scalaj.http.{ Http, HttpResponse }
import ujson._

case class SlackMessage(attachments: Js.Arr) {
  val channelUrl: String = sys.env("SLACK_CHANNEL_URL")

  def send = {
    val data = ujson.write(Js.Obj("attachments" -> attachments))
    Http(channelUrl)
      .timeout(connTimeoutMs = 5000, readTimeoutMs = 30000)
      .postData(data).header("content-type", "application/json")
      .asString
  }
}

