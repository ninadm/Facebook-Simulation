package Models

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol

/**
  * Created by Ninad on 28-11-2015.
  */
object UserAlbum extends DefaultJsonProtocol {
  var allAlbums = List[Albums]()
  implicit val albumFormat = jsonFormat4(Albums)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Albums])))
  def toJson(albumList: List[Albums]): String = Serialization.writePretty(albumList)
  def toJson(album: Albums): String = Serialization.writePretty(album)
}
