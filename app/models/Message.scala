package models

import java.util.Base64

import play.api.mvc.PathBindable
import play.api.libs.json._
import java.security.MessageDigest

/**
 * Represents a User, with his name and favorite color.
 */
case class User(name:String, color:String) {
  /**
   * Transform this user object to json with a key
   */
  def toJson:JsValue = Json.obj(
    "name" -> name,
    "color" -> color,
    "key" -> calculateKey()
  )

  /**
   * Calculates the key of this user using the secret and the super secure MD5 hash function.
   * Obviuslly, don't use this stupid system in the Real World.
   */
  private def calculateKey() = {
    MessageDigest.getInstance("MD5")
      .digest((name + User.secret + color).getBytes("UTF-8")).map("%02x".format(_)).mkString("")
  }
}

object User {

  /**
   * Used to generate the key of every user
   */
  private val secret = "-95Pf#6Nf09s6`9G029Gy+A1@6>RDñ"

  /**
   * Transform a json into a User. If the key is wrong, an exception is throwed.
   */
  def apply(json: JsValue):User = {
    val name = (json \ "name").as[String]
    val color = (json \ "color").as[String]
    val key = (json \ "key").as[String]
    val u = User(name, color)
    require(key == u.calculateKey())
    u
  }

  /**
   * Allow the serialization of an User object as a path param. If anything is wrong (with decoding,
   * json parsing or key checking) the client receives a Bad Request. It's a silly security mechanism,
   * but works for me ;)
   */
  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[User] {
    override def bind(key: String, value: String): Either[String, User] = {
      try {
        Right(User(Json.parse(Base64.getDecoder.decode(value))))
      } catch {
        case e:Exception => Left("Who R U?") //Bad Request
      }
    }

    override def unbind(key: String, user: User): String = {
      Base64.getEncoder.encodeToString(Json.stringify(user.toJson).getBytes())
    }
  }
}

/**
 * Represents a texr message from an user
 */
case class Message(user:User, text:String)
