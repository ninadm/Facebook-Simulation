/**
  * Created by Ninad on 21-11-2015.
  */

//import akka.actor.Status.Success
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants

import Models.PictureList._
import Models.UserAlbum._
import Models._
import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.reflect.internal.util.Statistics
import scala.util.{Random, Success, Failure}
import spray.httpx.encoding.{Deflate, Gzip}
import spray.client.pipelining._
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.SprayJsonSupport._
import User._
import Helper._

import scala.concurrent.Future
import scala.concurrent.duration
import scalaz.Order

case class CreateUser(userId: Int, emailID: String, name: String, age: Int, sex: String, city: String, friendList: Map[String, String], totalNumOfUsers: Int)
case class SendFriendRequests(from: Int, to: Int)
case class InitializeSimulator(numOfUsers: Int, userRefs: List[ActorRef])
case class AcceptRequest(from: Int, to: Int, permission: Int, master: ActorRef)
case class GetFriendList(requestedBy: Int, forUser: Int)
case class CreatePost(postCreatorId: Int)
case class LikeUserPost(postLikerUser: Int, forPost: Int)
case class CreatePageForUser(pageCreator: Int)
case class GetPagesForUser(requestedBy: Int, userId: Int)
case class GetSpecificPageRequestedByUser(requestedBy: Int, userId: Int, pageId: Int)
case class GetRequestedUserProfile(requestedBy: Int, userId: Int)
case class AddPicture(addedBy: Int)
case class AddAlbum(addedBy: Int)
case class GetAlbumForUser(requestedBy: Int, requestedFor: Int)
case class GetSpecificAlbumForAUser(requestedBy: Int, requestedFor: Int, requestedAlbumId: Int)
case class AddPhotosToAlbum(requestedBy: Int, requestedAlbumId: Int, requestedPictureId: Int)
case class GetAllPicturesById(requestedBy: Int, requestedFor: Int)
case class GetPictureRequestedUser(requestedBy: Int, requestedFor: Int, requestedPictureId: Int)
case object GetUsers
object Client {
  def main(args: Array[String]): Unit = {


    implicit val system = ActorSystem("FacebookSimulator")
    import system.dispatcher // execution context for futures

    val masterActor = system.actorOf(Props[Master])
    val numOfUsers = Helper.Statistics.NumberofUsers
    var userActors: List[ActorRef] = Nil

    for (i <- 1 to (numOfUsers) by +1) {
      userActors ::= system.actorOf(Props[UserActor])
    }
    masterActor ! InitializeSimulator(numOfUsers, userActors)
  }
}

class UserActor extends Actor {
  implicit val system = context.system
  import context._
  import Models.UserPosts._
  import Models.PageList._
  import Models.Page
  private var scheduler: Cancellable = _
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  var numberOfUserRequestsCompleted = 0
  val rnd = new Random()
  def receive = {
    case AcceptRequest(from, to, permission, master) => {
      //implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:8181/AcceptFriendRequest?from=${from.toString()}&to=${to.toString()}&permission=${permission.toString()}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
    case GetFriendList(requestedBy, forUser) => {
      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:8181/ShowFriendList?requestedBy=${requestedBy.toString()}&forUser=${forUser.toString()}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
    case CreatePost(postCreator) => {
      val post_id = rnd.nextInt(10000)
      val content = Random.alphanumeric.take(200).mkString
      val date = DateTime.now.toString();
      val likes = List[Int]()
      val comments = List[Int]()
      val post_visibility = rnd.nextInt(3).toString()
      val page_post = if(rnd.nextInt(100) % 2 == 0) true else false
      val page_id = if(page_post && allPosts.size > 0) rnd.nextInt(allPosts.size) else 0

      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:8181/createPost/${postCreator.toString()}",
        PostStructure(post_id, postCreator, content, date, likes, comments, post_visibility, page_post, page_id)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          //if(page_post) println("Created as a page post!")
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case LikeUserPost(userId, postId) => {
      implicit val timeout = Timeout(Duration.create(120, TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/likePost/${userId.toString()}/${postId.toString()}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case CreatePageForUser(pageCreator) => {
      val owner_id = pageCreator
      val page_id = rnd.nextInt(10000)
      val title = Random.alphanumeric.take(20).mkString
      val description = Random.alphanumeric.take(200).mkString
      val posts: List[Int] = List()
      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      //println("In create Page!")
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/createPage/${pageCreator.toString()}",
        Page(page_id, owner_id, title, description, posts)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          //println("In create Page success!")
          println(response.entity.asString)
        }

        case Failure(error) => {

          println(error)
        }
      }
    }

    case GetPagesForUser(requestedBy, userId) => {
      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/getAllPagesForUser/${requestedBy}/${userId}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1

          println(response.entity.asString)
        }

        case Failure(error) => {

          println(error)
        }
      }
    }

    case GetSpecificPageRequestedByUser(requestedBy, userId, pageId) => {
      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/getSpecificPageForUser/${requestedBy}/${userId}/${pageId}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1

          println(response.entity.asString)
        }

        case Failure(error) => {

          println(error)
        }
      }
    }

    case GetRequestedUserProfile(requestedBy, requestedFor) => {
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/getProfileRequestedByUser/${requestedBy}/${requestedFor}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }
        case Failure(error) => {
          println(error)
        }
      }
    }

    case AddPicture(addedBy) => {
      println("adding Picture")
      var picId: Int = allPictures.size
      var userId: Int = addedBy
      var name: String = Random.alphanumeric.take(20).mkString
      var url: String = "http://PictureLibrary/" + Random.alphanumeric.take(20).mkString
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/putPicture/${addedBy}",
        Picture(picId, userId, name, url)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1

          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case AddAlbum(addedBy) => {
      var albumId: Int = allAlbums.size
      var userId: Int = addedBy
      var name: String = Random.alphanumeric.take(20).mkString
      var pictureList: List[Int] = List()
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/createNewAlbum/${addedBy}",
        Albums(albumId, userId, name, pictureList)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
    case GetAlbumForUser(requestedBy, requestedFor) => {
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/allAlbums/${requestedBy}/${requestedFor}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
    case GetSpecificAlbumForAUser(requestedBy, requestedFor, requestedAlbumId) => {
      var albumId: Int = rnd.nextInt(allAlbums.size)
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/specificAlbum/${requestedBy}/${requestedFor}/${albumId}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case AddPhotosToAlbum(requestedBy, requestedAlbumId, requestedPictureId) => {
      var albumId: Int = rnd.nextInt(allAlbums.size)
      var userId: Int = requestedBy
      var picId: Int = rnd.nextInt(allPictures.size)
      var pictureList: List[Int] = List()
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/putPictureToAlbum/${userId}/${albumId}/${picId}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case GetAllPicturesById(requestedBy, requestedFor) => {
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/allPictures/${requestedBy}/${requestedFor}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }

    case GetPictureRequestedUser(requestedBy, requestedFor, requestedPictureId) => {
      var picId: Int = rnd.nextInt(allPictures.size)
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/specificPicture/${requestedBy}/${requestedFor}/${picId}"))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
  }
}

class Master extends Actor {
  implicit val system = context.system
  import context._
  import UserList._
  import FriendRequests._
  import Models.UserPosts._
  import Models.UserAlbum._
  import Models.PictureList._
  private var scheduler: Cancellable = _
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  var numberOfUserRequestsCompleted = 0
  val rnd = new Random()
  var allUsers: List[ActorRef] = Nil
  def receive = {
    case InitializeSimulator(numOfUsers, userRefs) => {
      allUsers = userRefs
      for (i <- 1 to (numOfUsers) by +1) {
        val name = "user" + i.toString()
        val emailID = name + "@gmail.com"
        val age = rnd.nextInt((80 - 12) + 1) + 12
        val sex = Gender.apply(i % 2).toString()
        val city = "city" + rnd.nextInt((100 - 1) + 1) + 1
        val friendList: Map[String, String] = Map()
        self ! CreateUser(i, emailID, name, age, sex, city, friendList, numOfUsers)
        //context.system.scheduler.schedule(Duration.create(0, TimeUnit.MILLISECONDS),
          //Duration.create(5000, TimeUnit.MILLISECONDS))(printStats())
      }
    }

    case CreateUser(userId, emailID, name, age, sex, city, friendList, totalNumOfUsers) => {
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/addUser", User(userId, emailID, name, age, sex, city, friendList)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          println(response.entity.asString)
          numberOfUserRequestsCompleted += 1
          if (numberOfUserRequestsCompleted == totalNumOfUsers) {
            //scheduler = context.system.scheduler.scheduleOnce(Duration.create(1000, TimeUnit.MILLISECONDS), self, GetUsers)
            val numOfFriendsPerUser = Helper.Statistics.InitialFriendsToSeed
            //println(s"Number of Friends per user ${numOfFriendsPerUser}")
            for(j <-  1 to (totalNumOfUsers) by +1){
              for (i <- 1 to (numOfFriendsPerUser) by +1) {
                self ! SendFriendRequests(rnd.nextInt(totalNumOfUsers), rnd.nextInt(totalNumOfUsers))
              }
            }
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(10000, TimeUnit.MILLISECONDS))(sendFriendRequestSchedule(self, totalNumOfUsers))
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(monitorPostCreationSchedule())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(viewFriendListOnSchedule())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(likePostsOnSchedule())
            for (i <- 1 to 50 by +1) {
              startPageCreation();
            }
//            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
//              Duration.create(100, TimeUnit.MILLISECONDS))(startPageCreation())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(startGetAllPages())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(startGetSpecificPages())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(startFetchingProfiles())

            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(startAddingPicture())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(startAddingAlbum())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(addPhotoToAlbum())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(getAllAlbumsForAUser())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(getAllPicturesByUserId())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(getPictureForRequestedUser())
            context.system.scheduler.schedule(Duration.create(2000, TimeUnit.MILLISECONDS),
              Duration.create(2, TimeUnit.MILLISECONDS))(getSpecificAlbumForRequestedUser())
          }
        }

        case Failure(error) => {
          println(error)
          numberOfUserRequestsCompleted += 1
        }
      }
    }
    case GetUsers => {
      val responseF: Future[HttpResponse] = pipeline(Get(s"http://localhost:${Helper.Statistics.portNumber}/list/allUsers"))
      responseF onComplete {
        case Success(response) => {

          println(response.entity.asString)
          Helper.Statistics.NumberOfSuccessfulRequest += 1
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
    case SendFriendRequests(from, to) => {
      implicit val timeout = Timeout(Duration.create(120,TimeUnit.SECONDS))
      val responseF: Future[HttpResponse] = pipeline(Post(s"http://localhost:${Helper.Statistics.portNumber}/SendFriendRequest", FriendListMapping(from, to)))
      responseF onComplete {
        case Success(response) => {
          Helper.Statistics.NumberOfSuccessfulRequest += 1
          val permission = rnd.nextInt(100)
          allUsers(to) ! AcceptRequest(from, to, permission % 2, self)
          println(response.entity.asString)
        }

        case Failure(error) => {
          println(error)
        }
      }
    }
  }

  def monitorPostCreationSchedule(): Unit = {
    val postCreationInitiator: Int = rnd.nextInt(allUsers.length)
    val forUser: Int = rnd.nextInt(allUsers.length)
    allUsers(postCreationInitiator) ! CreatePost(postCreationInitiator)
  }

  def sendFriendRequestSchedule(actor: ActorRef, totalNumOfUsers: Int): Unit = {
    actor ! SendFriendRequests(rnd.nextInt(totalNumOfUsers), rnd.nextInt(totalNumOfUsers))
  }

  def viewFriendListOnSchedule(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val forUser: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetFriendList(requestedBy, forUser)
  }

  def likePostsOnSchedule(): Unit = {
    val postLikerUser: Int = rnd.nextInt(allUsers.length)
    val forPost: Int = if (allPosts.length != 0) rnd.nextInt(allPosts.length) else 0
    allUsers(postLikerUser) ! LikeUserPost(postLikerUser, forPost)
  }

  def startPageCreation(): Unit = {
    val pageCreator: Int = rnd.nextInt(allUsers.length)
    allUsers(pageCreator) ! CreatePageForUser(pageCreator)
  }

  def startGetAllPages(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetPagesForUser(requestedBy, requestedFor)
  }

  def startGetSpecificPages(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val pageId: Int = rnd.nextInt(allPosts.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetSpecificPageRequestedByUser(requestedBy, requestedFor, pageId)
  }

  def startFetchingProfiles(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetRequestedUserProfile(requestedBy, requestedFor)
  }

  def printStats(): Unit = {
    println(s"Number of Requests Completed Per 5 Second: ${Helper.Statistics.NumberOfSuccessfulRequest}")
    Helper.Statistics.NumberOfSuccessfulRequest = 0
  }

  def startAddingPicture(): Unit = {
    val addedBy: Int = rnd.nextInt(allUsers.length)
    allUsers(addedBy) ! AddPicture(addedBy)
  }

  def startAddingAlbum(): Unit = {
    val addedBy: Int = rnd.nextInt(allUsers.length)
    allUsers(addedBy) ! AddAlbum(addedBy)
  }

  def getAllAlbumsForAUser(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetAlbumForUser(requestedBy, requestedFor)
  }

  def getSpecificAlbumForRequestedUser(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    val requestedAlbumId: Int = rnd.nextInt(allAlbums.length)
    allUsers(requestedBy) ! GetSpecificAlbumForAUser(requestedBy, requestedFor, requestedAlbumId)
  }

  def addPhotoToAlbum(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedAlbumId: Int = rnd.nextInt(allAlbums.length)
    val requestedPictureId: Int = rnd.nextInt(allPictures.length)
    allUsers(requestedBy) ! AddPhotosToAlbum(requestedBy, requestedAlbumId, requestedPictureId)
  }

  def getAllPicturesByUserId(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    allUsers(requestedBy) ! GetAllPicturesById(requestedBy, requestedFor)
  }

  def getPictureForRequestedUser(): Unit = {
    val requestedBy: Int = rnd.nextInt(allUsers.length)
    val requestedFor: Int = rnd.nextInt(allUsers.length)
    val requestedPictureId: Int = rnd.nextInt(allPictures.length)
    allUsers(requestedBy) ! GetPictureRequestedUser(requestedBy, requestedFor, requestedPictureId)
  }

}

