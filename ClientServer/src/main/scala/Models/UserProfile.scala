package Models

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol

/**
  * Created by Ninad on 30-11-2015.
  */
case class UserProfile(user_id: Int, emailID: String, name: String, age: Int, sex: String,
                  city: String, friendList: List[String], pages: List[String], posts: List[String])

object UserProfileJsonProtocol extends DefaultJsonProtocol {

  implicit val userFormat = jsonFormat9(UserProfile)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[UserProfile])))
  def toJson(userProfile: UserProfile): String = Serialization.writePretty(UserProfile)
}