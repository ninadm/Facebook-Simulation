package Models

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol

/**
  * Created by Ninad on 28-11-2015.
  */
object UserList extends DefaultJsonProtocol {
    var allUsers = List[User]()
    implicit val userFormat = jsonFormat7(User)
    private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[User])))
    def toJson(userList: List[User]): String = Serialization.writePretty(userList)
    def toJson(user: User): String = Serialization.writePretty(user)
}
