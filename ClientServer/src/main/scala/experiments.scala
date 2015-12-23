/**
  * Created by Ninad on 20-11-2015.
  */
case class Dummy(var id: Int,  var friendList: Int)
object Margin extends Enumeration {
  type Margin = Value
  val TOP, BOTTOM, LEFT, RIGHT = Value
}

object experiments extends App{
  println(Margin.apply(0))
  println(Margin.maxId)
  var slist = List[Dummy](Dummy(1,11), Dummy(2,12));
  println("hehe1")
  slist.foreach(println)
  var userA = (slist find {user => user.id == 1}).getOrElse(null)
  println(userA)
  userA.friendList = 13
  println("hehe2")
  slist.foreach(println)
  println("hehe3")
  slist = slist.filterNot(a => (a.id == 2 && a.friendList == 12))
  slist.foreach(println)

}

