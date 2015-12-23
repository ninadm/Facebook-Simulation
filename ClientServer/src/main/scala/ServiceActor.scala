/**
  * Created by Ninad on 20-11-2015.
  */

import Models._
import akka.actor.{Props, ActorRef, ActorSystem, Actor}
import spray.routing._
import spray.http._
import spray.httpx.SprayJsonSupport._
import UserList._
import FriendRequests._
import UserProfileJsonProtocol._
import Models._
import Helper._

import scala.collection.mutable

sealed trait GenericUser
case class AddThisUser(userToBeAdded: User, ctx: RequestContext) extends GenericUser
case class GetSpecificUser(index: Int, ctx: RequestContext) extends GenericUser
case class GetAllUsers(ctx: RequestContext) extends GenericUser
case class GetAllFriendRequests(ctx: RequestContext) extends GenericUser
case class AddPost(newPost: PostStructure, ctx: RequestContext) extends GenericUser
case class CreatePage(page: Page, ctx: RequestContext) extends GenericUser

sealed trait SpecificUser
case class SendFriendRequest(newFriendRequest: FriendListMapping, ctx: RequestContext) extends SpecificUser
case class AcceptFriendRequest(byId: String, ofId: String,permission: Int, ctx: RequestContext) extends SpecificUser
case class DeleteFriendRequest(byId: String, ofId: String, ctx: RequestContext) extends SpecificUser
case class ShowFriendList(requestedBy: String, forUser: String, ctx: RequestContext) extends SpecificUser
case class GetAllPostsPerUser(userId: Int, ctx: RequestContext) extends SpecificUser
case class GetAllPostsForRequestedUser(requestedByUserID: Int, requestedForUserId: Int, ctx: RequestContext) extends SpecificUser
case class GetTopPostsForRequestedUser(requestedByUserID: Int, requestedForUserId: Int, limit: Int, ctx: RequestContext) extends SpecificUser
case class LikePost(userId: Int, postId: Int, ctx: RequestContext) extends SpecificUser
case class GetSpecificPageForUser(requesterId: Int, userId: Int, pageId: Int, ctx: RequestContext) extends SpecificUser
case class GetAllPagesForUser(requesterId: Int, userId: Int, ctx: RequestContext) extends SpecificUser
case class GetUserProfile(requesterId: Int, userId: Int, ctx: RequestContext) extends SpecificUser
case class PutPicture(picture: Picture, ctx: RequestContext) extends SpecificUser
case class CreateAlbum(album: Albums, ctx: RequestContext) extends SpecificUser
case class GetAllAlbumsForRequestedUser(requestedByUserID: Int, requestedForUserId: Int, ctx: RequestContext) extends SpecificUser
case class GetSpecificAlbumForRequestedUser(requestedByUserID: Int, requestedForUserId: Int, album_ID: Int, ctx: RequestContext) extends SpecificUser
case class AddPhotoToAlbum(userId: Int, album_ID: Int, picId: Int, ctx: RequestContext) extends SpecificUser
case class GetAllPicturesByUserId(requestedBy: Int, userId: Int, ctx: RequestContext) extends SpecificUser
case class GetPictureForRequestedUser(requestedByUserID: Int, requestedForUserId: Int, picId: Int, ctx: RequestContext) extends SpecificUser
// simple actor that handles the routes.
object ServiceActor extends App with SimpleRoutingApp {

  implicit val actorSystem = ActorSystem("MainSystem")
  var userOperationsActor: ActorRef = actorSystem.actorOf(Props[UserOperations])
  var specificUserOperationsActor: ActorRef = actorSystem.actorOf(Props[SpecificUserOperations])
  import UserPosts._
  import PageList._
  import UserList._
  import UserAlbum._
  import PictureList._
  import Picture._
  import User._
  import Albums._

  lazy val albumGetRoute = get {
      path("allAlbums" / IntNumber / IntNumber) { (requestedByUserID, requestedForUserId) =>
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => specificUserOperationsActor ! GetAllAlbumsForRequestedUser(requestedByUserID, requestedForUserId, ctx)
        }
      } ~
      path("specificAlbum" / IntNumber / IntNumber / IntNumber) { (requestedByUserID, requestedForUserId, album_ID) =>
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => specificUserOperationsActor ! GetSpecificAlbumForRequestedUser(requestedByUserID, requestedForUserId, album_ID, ctx)
        }
      } ~
      path("allPictures" / IntNumber / IntNumber) { (requested_by, user_id) =>
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => specificUserOperationsActor ! GetAllPicturesByUserId(requested_by, user_id, ctx)
        }
      } ~
      path("specificPicture" / IntNumber / IntNumber / IntNumber) { (requestedByUserID, requestedForUserId, pic_ID) =>
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => specificUserOperationsActor ! GetPictureForRequestedUser(requestedByUserID, requestedForUserId, pic_ID, ctx)
        }
      }
  }


  val albumPostRoute = post {
    path("putPicture" / IntNumber) { user_id =>
      entity(as[Picture]) { picture =>
        ctx => specificUserOperationsActor ! PutPicture(picture, ctx)
      }
    } ~
      path("putPictureToAlbum" / IntNumber / IntNumber/ IntNumber) { (user_id, album_ID, pic_ID) =>
        ctx => specificUserOperationsActor ! AddPhotoToAlbum(user_id, album_ID, pic_ID, ctx)
      } ~
      path("createNewAlbum" / IntNumber) { user_id =>
        entity(as[Albums]) { album =>
          ctx => specificUserOperationsActor ! CreateAlbum(album, ctx)
        }
      }
  }

  lazy val pageGetRoute = get {
    path("getSpecificPageForUser" / IntNumber/ IntNumber / IntNumber) { (requester_id, user_id, page_id) =>
      respondWithMediaType(MediaTypes.`application/json`) {
        ctx => specificUserOperationsActor ! GetSpecificPageForUser(requester_id, user_id, page_id, ctx)
      }
    } ~
      path("getAllPagesForUser" /  IntNumber/ IntNumber) { (requester_id, user_id) =>
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => specificUserOperationsActor ! GetAllPagesForUser(requester_id, user_id, ctx)
        }
      }
  }

  val pagePostRoute = post {
    path("createPage" / IntNumber) { user_id =>
      entity(as[Page]) { newPage =>
        ctx => userOperationsActor ! CreatePage(newPage, ctx)
      }
    }
  }

  val getProfileRoute = get {
    path("getProfileRequestedByUser" / IntNumber/ IntNumber) { (requester_id, user_id) =>
      respondWithMediaType(MediaTypes.`application/json`) {
        ctx => specificUserOperationsActor ! GetUserProfile(requester_id, user_id, ctx)
      }
    }
  }

  lazy val userGetRoute = get {
    path("allposts" / IntNumber) { user_id =>
      respondWithMediaType(MediaTypes.`application/json`) {
        ctx => specificUserOperationsActor ! GetAllPostsPerUser(user_id, ctx)
      }
    } ~
    path("allposts" / IntNumber / IntNumber) { (requestedByUserID, requestedForUserId) =>
      respondWithMediaType(MediaTypes.`application/json`) {
        ctx => specificUserOperationsActor ! GetAllPostsForRequestedUser(requestedByUserID, requestedForUserId, ctx)
      }
    } ~
    path("topPosts" / IntNumber / IntNumber / IntNumber) { (requestedByUserID, requestedForUserId, limit) =>
      respondWithMediaType(MediaTypes.`application/json`) {
        ctx => specificUserOperationsActor ! GetTopPostsForRequestedUser(requestedByUserID, requestedForUserId, limit, ctx)
      }
    }
  }

   val userPostRoute = post {
    path("createPost" / IntNumber) { user_id =>
      entity(as[PostStructure]) { newPost =>
        ctx => specificUserOperationsActor ! AddPost(newPost, ctx)
      }
    } ~
      path("likePost" / IntNumber / IntNumber) { (userId, postId) =>
        ctx => specificUserOperationsActor ! LikePost(userId, postId, ctx)
      }
  }

  startServer(interface = "localhost", port = 8181) {
    get {
      path("example1") {
        ctx => ctx.complete("Welcome!")
      } ~
      path("list" / "allUsers") {
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => userOperationsActor ! GetAllUsers(ctx)
        }
      } ~
      path("get" / IntNumber / "Details") { index =>
          ctx => userOperationsActor ! GetSpecificUser(index, ctx)
      } ~
      path("list" / "friendRequests") {
        respondWithMediaType(MediaTypes.`application/json`) {
          ctx => userOperationsActor ! GetAllFriendRequests(ctx)
        }
      }
    } ~
      post {
        path("addUser") {
          entity(as[User]) { newUser =>
            ctx => userOperationsActor ! AddThisUser(newUser, ctx)
          }
        } ~
        path("SendFriendRequest") {
          entity(as[FriendListMapping]) { newFriendRequest =>
            ctx => specificUserOperationsActor ! SendFriendRequest(newFriendRequest, ctx)
          }
        } ~
        path("AcceptFriendRequest") {
          parameters("from".as[String], "to".as[String], "permission".as[Int]) { (from, to, perm) =>
            ctx => specificUserOperationsActor ! AcceptFriendRequest(from, to, perm, ctx)
          }
        } ~
        path("DeleteFriendRequest") {
          parameters("from".as[String], "to".as[String]) { (from, to) =>
            ctx => specificUserOperationsActor ! DeleteFriendRequest(from, to, ctx)
          }
        } ~
          path("ShowFriendList") {
          parameters("requestedBy".as[String], "forUser".as[String]) { (requestedBy, forUser) =>
            ctx => specificUserOperationsActor ! ShowFriendList(requestedBy, forUser, ctx)
          }
        }
      } ~ userGetRoute ~ userPostRoute ~ pageGetRoute ~ pagePostRoute ~ getProfileRoute ~ albumGetRoute ~ albumPostRoute
  }
}

class UserOperations extends Actor {
  def receive = {
    case GetAllUsers(ctx) => {
      ctx.complete(UserList.toJson(allUsers))
    }

    case GetAllFriendRequests(ctx) => {
      ctx.complete(FriendRequests.toJson(pendingFriendRequests))
    }

    case GetSpecificUser(id, ctx) => {
      var specificUser: User = null
      for (user <- allUsers) {
        if(user.user_id == id) {
          specificUser = user
        }
      }
      if (specificUser != null)
        ctx.complete(UserList.toJson(specificUser))
      else
        ctx.complete("User Not Found!")
    }

    case AddThisUser(userToBeAdded, ctx) => {
      //userToBeAdded.user_id = allUsers.size
      //userToBeAdded.sex = Gender.apply(userToBeAdded.sex.toInt).toString()
      allUsers = userToBeAdded :: allUsers
      ctx.complete(s"User ${userToBeAdded.name} added!")
    }

    case CreatePage(page, ctx) => {
      PageList.allPages = page :: PageList.allPages
      ctx.complete(s"Page ${page.title} added by user ${page.owner_id}!")
    }
  }
}

class SpecificUserOperations extends Actor {

  def receive = {
    case SendFriendRequest(friendRequest, ctx) => {
      if (friendRequest.from != friendRequest.to) {
        pendingFriendRequests = friendRequest :: pendingFriendRequests
        ctx.complete("Friend Request Sent!")
      }
      else {
        ctx.complete("Invalid Friend Request!")
      }
    }

    case AcceptFriendRequest(byId, ofId, permission, ctx) => {
      val ofUser = (allUsers find { user => user.user_id == ofId.toInt }).getOrElse(null)
      val byUser = (allUsers find { user => user.user_id == byId.toInt }).getOrElse(null)
      if (ofUser != null && byUser != null) {
        if (!ofUser.friendList.contains(byId))
          ofUser.friendList = ofUser.friendList + (byId -> FriendListVisibility.apply(permission).toString())
        if (!byUser.friendList.contains(ofId))
          byUser.friendList = byUser.friendList + (ofId -> FriendListVisibility.apply(permission).toString())
        pendingFriendRequests = pendingFriendRequests.filterNot(fr => fr.from == byId.toInt && fr.to == ofId.toInt)
        ctx.complete("Friend Request Accepted!")
      } else
        ctx.complete("Invalid Friend Request!")
    }

    case DeleteFriendRequest(byId, ofId, ctx) => {
      pendingFriendRequests = pendingFriendRequests.filterNot(fr => fr.from == byId.toInt && fr.to == ofId.toInt)
      ctx.complete("Friend Request Deleted!")
    }

    case ShowFriendList(requestedBy, forUser, ctx) => {
        ctx.complete(FriendList.toJson(makeFriendList(requestedBy, forUser)))
    }

    //Do this only for checking Need not be a part of API
    case GetAllPostsPerUser(userId, ctx) => {
      val postsOfUser = (UserPosts.allPosts find { user => user.owner_id == userId }).getOrElse(null)
      if(postsOfUser != null) {
        ctx.complete(UserPosts.toJson(postsOfUser))
      } else {
        ctx.complete("No posts found for the user!")
      }
    }

    case GetAllPostsForRequestedUser(requestedByUserID, requestedForUserId, ctx) => {
      //Show only public post of that user . Include a visibility bit in created post.
      // check that as well and show public + posts visible to friends if they are friends
      val areFriends: Boolean = checkIfFriends(requestedByUserID, requestedByUserID)
      var posts = (UserPosts.allPosts filter { user => user.post_visibility == "PUBLIC" })
      if(areFriends) {
        posts = posts ::: (UserPosts.allPosts filter { user => user.post_visibility == "FRIENDS" })
        if (posts.size != 0)
          ctx.complete(UserPosts.toJson(posts))
        else
          ctx.complete("No posts found for the user!")
      }
    }

    case GetTopPostsForRequestedUser(requestedByUserID, requestedForUserId, limit, ctx) => {

    }

    case AddPost(newPost, ctx) => {
      newPost.post_id = UserPosts.allPosts.size;
      newPost.date_of_creation = DateTime.now.toString();
      newPost.post_visibility = PostVisibility.apply(newPost.post_visibility.toInt).toString()
      UserPosts.allPosts = newPost :: UserPosts.allPosts
      ctx.complete("New Post Added!")
    }

    case LikePost(userId, postId, ctx) => {
      val likeForPost = (UserPosts.allPosts find { post => post.post_id == postId }).getOrElse(null)
      if(likeForPost != null) {
        likeForPost.like_for_posts = userId :: likeForPost.like_for_posts
      }
      ctx.complete(s"Post ${userId} liked post ${postId} !")
    }

    case GetAllPagesForUser(requesterId, userId, ctx) => {
      //use requesterId later for permission
      val pagesOfUser = (PageList.allPages find { page => page.owner_id == userId }).getOrElse(null)
      if(pagesOfUser != null) {
        ctx.complete(PageList.toJson(pagesOfUser))
      } else {
        ctx.complete("No pages found for the user!")
      }
    }

    case GetSpecificPageForUser(requesterId, userId, pageId, ctx) => {
      //use requesterId later for permission
      val specificPage = (PageList.allPages find { page => page.owner_id == userId }).getOrElse(null)
      if(specificPage != null) {
        ctx.complete(PageList.toJson(specificPage))
      } else {
        ctx.complete(s"No page with ID ${pageId} found!")
      }
    }

    case GetUserProfile(requesterId, userId, ctx) => {
      val friendList = makeFriendList(requesterId.toString(), userId.toString())
      val friendNames = friendList.map(_.name_of_friend)
      var posts = UserPosts.allPosts filter { user => user.post_visibility == "PUBLIC" }
      val postsNames = posts.map(_.post_content).take(10)
      if(checkIfFriends(requesterId, userId)) {
        posts = posts ::: (UserPosts.allPosts filter { user => user.post_visibility == "FRIENDS" })
      }
      val pages = ( PageList.allPages filter { page => page.owner_id == userId })
      val pagesNames = pages.map(_.title).take(10)
      val user = (allUsers find {user => user.user_id == userId }).getOrElse(null)
      if(user != null) {
        ctx.complete(UserProfileJsonProtocol.toJson(UserProfile(user.user_id, user.emailID, user.name, user.age,
          user.sex, user.city, friendNames, pagesNames, postsNames)))
      } else
        {
          ctx.complete("Invalid Profile Requested!")
        }
    }

    case PutPicture(picture, ctx) => {
      //picture.pic_ID = PictureList.allPictures.size;
      println("adding Picture")
      PictureList.allPictures = picture :: PictureList.allPictures
      ctx.complete("New Picture Added!")
    }

    case CreateAlbum(album, ctx) => {
      UserAlbum.allAlbums = album :: UserAlbum.allAlbums
      ctx.complete("New Album Added!")
    }

    case GetAllAlbumsForRequestedUser(requestedByUserID, requestedForUserId, ctx) => {
      val albumsOfUser = (UserAlbum.allAlbums find { album => album.user_id == requestedForUserId }).getOrElse(null)
      if(albumsOfUser != null) {
        ctx.complete(UserAlbum.toJson(albumsOfUser))
      } else {
        ctx.complete("No Albums found for the user!")
      }
    }

    case GetSpecificAlbumForRequestedUser(requestedByUserID, requestedForUserId, album_ID, ctx) => {
      var albumsOfUser = (UserAlbum.allAlbums find { album => (album.user_id == requestedForUserId
        && album.album_ID == album_ID)}).getOrElse(null)
      albumsOfUser = (UserAlbum.allAlbums find { album => album.album_ID == album_ID }).getOrElse(null)
      if(albumsOfUser != null) {
        ctx.complete(UserAlbum.toJson(albumsOfUser))
      } else {
        ctx.complete("No album with specified ID is found for the user!")
      }
    }

    case AddPhotoToAlbum(userId, albumId, picId, ctx) => {
      var albumsOfUser = (UserAlbum.allAlbums find { album => (album.user_id == userId &&
        album.album_ID == albumId)}).getOrElse(null)
      if (albumsOfUser != null ) {
        if (!albumsOfUser.pictureList.contains(picId))
          albumsOfUser.pictureList = picId :: albumsOfUser.pictureList
        ctx.complete("Picture Added to the Album succesfully!")
      } else
        ctx.complete("Picture Could not be added to the album!")
    }

    case GetAllPicturesByUserId(requestedByUserID, userId, ctx) => {
      val picturesOfUser = (PictureList.allPictures find { picture => picture.user_id == userId }).getOrElse(null)
      if(picturesOfUser != null) {
        ctx.complete(PictureList.toJson(picturesOfUser))
      } else {
        ctx.complete("No pictures found for the user!")
      }
    }

    case GetPictureForRequestedUser(requestedByUserID, requestedForUserId, picId, ctx) => {
      var picturesOfUser = (PictureList.allPictures find { picture => (picture.user_id == requestedForUserId
        && picture.pic_ID == picId)}).getOrElse(null)
      if(picturesOfUser != null) {
        ctx.complete(PictureList.toJson(picturesOfUser))
      } else {
        ctx.complete("No pictures found for the supplied Id!")
      }
    }
  }

  def makeFriendList(requestedBy: String, forUser: String): List[Friend] = {
    val forThisUser = (allUsers find { user => user.user_id == forUser.toInt }).getOrElse(null)
    val requestedByUser = (allUsers find { user => user.user_id == requestedBy.toInt }).getOrElse(null)
    var friendListForUser = List[Friend]();
    if (forThisUser != null && requestedByUser != null) {
      if (forThisUser.friendList.contains(requestedBy) && forThisUser.friendList(requestedBy) == FriendListVisibility(0).toString()) {
        forThisUser.friendList.keys.foreach { friend_user_id =>
          friendListForUser = Friend(requestedBy, friend_user_id, ((allUsers find { user => user.user_id == friend_user_id.toInt }).get).name) :: friendListForUser
        }
      }
    }
    return friendListForUser
  }

  def checkIfFriends(userOne: Int, userTwo: Int): Boolean = {
    val user = (allUsers find { user => user.user_id == userOne }).getOrElse(null)
    if (user != null && user.friendList.contains(userTwo.toString)) {
      return true
    }
    return false
  }
}