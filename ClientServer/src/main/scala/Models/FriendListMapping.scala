package Models

/**
  * Created by Ninad on 22-11-2015.
  */
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json._

case class FriendListMapping(from: Int, to: Int)

object FriendRequests extends DefaultJsonProtocol {
  var pendingFriendRequests = List[FriendListMapping]()
  implicit val frFormat = jsonFormat2(FriendListMapping)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[FriendListMapping])))
  def toJson(FRList: List[FriendListMapping]): String = Serialization.writePretty(FRList)
  def toJson(friendRequest: FriendListMapping): String = Serialization.writePretty(friendRequest)
}

object FriendListVisibility extends Enumeration {
  type FriendListVisibility = Value
  val SHOW_FRIENDLIST, DO_NOT_SHOW_FRIENDLIST = Value
}