package controllers

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import models.{User,Message}
import play.api.libs.json._
import scala.collection.mutable

/**
 * Every instance of ChatActor represents a client.
 */
class ChatActor(user:User, client: ActorRef, clientListActor:ActorRef) extends Actor {

  /**
   * Adds the client to the client list
   */
  override def preStart() = {
    clientListActor ! AddClient(user, client)
  }

  /**
   * Removes the client from the client list
   */
  override def postStop() = {
    clientListActor ! RemoveClient(user, client)
  }

  /**
   * Propagates the message received to all the clients
   * using the ClientListActor
   */
  def receive = {
    case json: JsValue => {
      val message = (json \ "message").as[String]
      clientListActor ! MessageAll(Message(user, message))
    }
  }
}

object ChatActor {

  /**
   * Singleton instance of a ClientListActor
   */
  val clientListActor = ActorSystem().actorOf(Props[ClientListActor])

  /**
   * Method invoked from the ChatController to initialize a
   * ChatActor
   */
  def props(user:User, outChannel: ActorRef) = {
    Props(new ChatActor(user, outChannel, clientListActor))
  }
}

/**
 * Represents the different type of messages that the ClientListActor
 * can receibe.
 */
sealed trait ClientListActorMsg{}

case class AddClient(val user:User, val client:ActorRef) extends ClientListActorMsg

case class RemoveClient(val user:User, val client:ActorRef) extends ClientListActorMsg

case class MessageAll(val message:Message) extends ClientListActorMsg

/**
 * This actor manages the list of clients. It's possible to add a client,
 * remove a client or send a message to all the clients.
 */
class ClientListActor extends Actor {

  /**
   * List (well, queue) of clients
   */
  private val clients = mutable.Queue[(User,ActorRef)]()

  /**
   * Adds a client, removes a client or sends a message to all the clients.
   */
  def receive = {

    case AddClient(user, newClient) => {

      System.out.println("Added client " + user.name)

      val newLogin = Json.obj(
        "type" -> "login",
        "user" -> user.name
      )
      clients.foreach( client => {
        client._2 ! newLogin

        val currentLogin = Json.obj(
          "type" -> "login",
          "user" -> client._1.name
        )
        newClient ! currentLogin
      })

      clients.enqueue((user,newClient))
    }

    case RemoveClient(u, c) => {

      System.out.println("Removed client " + u.name)

      clients.dequeueFirst(_ == (u,c))

      val logout = Json.obj(
        "type" -> "logout",
        "user" -> u.name
      )
      clients.foreach( client => client._2 ! logout);
    }

    case MessageAll(m) => {
      val json = Json.obj(
        "type" -> "message",
        "message" -> m.text,
        "user" -> m.user.name,
        "color" -> m.user.color
      )
      clients.foreach(client => client._2 ! json)
    }
  }
}