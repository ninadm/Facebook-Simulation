package Models

/**
  * Created by Ninad on 29-11-2015.
  */
case class Page(page_id: Int, owner_id: Int, title: String, description: String, posts: List[Int])
