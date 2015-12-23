package Models

/**
  * Created by Ninad on 23-11-2015.
  */
import org.json4s.{ShortTypeHints}
import org.json4s.native.Serialization
import spray.json._

case class Friend(user_id_of: String, user_id_of_friend: String, name_of_friend: String)
object FriendList extends DefaultJsonProtocol{
  var friends = List[Friend]()
  implicit val frFormat = jsonFormat3(Friend)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Friend])))
  def toJson(FRList: List[Friend]): String = Serialization.writePretty(FRList)
  def toJson(friend: Friend): String = Serialization.writePretty(friend)
}
