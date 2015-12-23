package Helper

/**
  * Created by Ninad on 24-11-2015.
  */
case class Comment(comment_text: String, date_of_comment: String, user_id: Int) {
  var commentText: String = comment_text
  var dateOfComment: String = date_of_comment
  var userId: Int = user_id
}
