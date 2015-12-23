package Models

import Helper._
import org.json4s.ShortTypeHints
import org.json4s.native.Serialization
import spray.json.DefaultJsonProtocol
/**
  * Created by Ninad on 24-11-2015.
  */
object PostVisibility extends Enumeration {
  type PostVisibility = Value
  val ONLY_ME, FRIENDS, PUBLIC = Value
}

case class PostStructure(var post_id: Int, owner_id: Int, post_content: String,  var date_of_creation: String, var like_for_posts: List[Int], comments_for_posts: List[Int], var post_visibility: String, is_page_post: Boolean, page_id: Int)

object UserPosts extends DefaultJsonProtocol {
  var allPosts = List[PostStructure]()
  //implicit val commentFormat = jsonFormat3(Helper.Comment)
  implicit val postFormat = jsonFormat9(PostStructure)
  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[PostStructure])))
  def toJson(postList: List[PostStructure]): String = Serialization.writePretty(postList)
  def toJson(post: PostStructure): String = Serialization.writePretty(post)
}
