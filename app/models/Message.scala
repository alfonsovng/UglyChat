package models

import java.util.Base64

import play.api.mvc.PathBindable
import play.api.libs.json._
import java.security.MessageDigest

case class User(name:String, color:String) {
  def toJson:JsValue = Json.obj(
    "name" -> name,
    "color" -> color,
    "key" -> User.getKey(this)
  )
}

object User {

  private val secret = "-95Pf#6Nf09s6`9G029Gy+A1@6>RDñ"

  def apply(json: JsValue):User = {
    val name = (json \ "name").as[String]
    val color = (json \ "color").as[String]
    val key = (json \ "key").as[String]
    val u = User(name, color)
    require(key == getKey(u))
    u
  }

  private def getKey(u:User) = {
    MessageDigest.getInstance("MD5").digest((u.name + secret + u.color).getBytes("UTF-8")).map("%02x".format(_)).mkString("")
  }

  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[User] {
    override def bind(key: String, value: String): Either[String, User] = {
      try {
        Right(User(Json.parse(Base64.getDecoder.decode(value))))
      } catch {
        case e:Exception => Left("Who R U?")
      }
    }

    override def unbind(key: String, user: User): String = {
      Base64.getEncoder.encodeToString(Json.stringify(user.toJson).getBytes())
    }
  }
}

case class Message(user:User, text:String)
