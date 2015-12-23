package Models

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol

object PictureList extends DefaultJsonProtocol {
  var allPictures = List[Picture]()
  implicit val pictureFormat = jsonFormat4(Picture)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Picture])))
  def toJson(userPictures: List[Picture]): String = Serialization.writePretty(userPictures)
  def toJson(picture: Picture): String = Serialization.writePretty(picture)
}
