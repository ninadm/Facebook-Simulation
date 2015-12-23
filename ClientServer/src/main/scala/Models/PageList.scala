package Models

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol
/**
  * Created by Ninad on 29-11-2015.
  */
object PageList extends DefaultJsonProtocol {
  var allPages = List[Page]()
  implicit val pageFormat = jsonFormat5(Page)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Page])))
  def toJson(pageList: List[Page]): String = Serialization.writePretty(pageList)
  def toJson(page: Page): String = Serialization.writePretty(page)
}

