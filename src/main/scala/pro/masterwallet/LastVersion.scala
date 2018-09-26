package pro.masterwallet

import scalaj.http.{ Http, HttpResponse }
import ujson._

object LastVersion {
  val rawPackageJsonUrl: String = "https://raw.githubusercontent.com/masterwallet/identity-server-js/master/package.json"

  def get: String = {
    val response: HttpResponse[String] = Http(rawPackageJsonUrl).timeout(connTimeoutMs = 5000, readTimeoutMs = 30000).asString
    val json = ujson.read(response.body)
    json.obj("version").str
  }
}
