package Models

/**
  * Created by Ninad on 28-11-2015.
  */
case class User(var user_id: Int, emailID: String, name: String, age: Int, var sex: String, city: String, var friendList: Map[String, String])
