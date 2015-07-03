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
 * Represents a client, with a name and an actor where send messages
 */
case class Client(val userName:String, val actor: ActorRef);

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
  private val clients = mutable.Queue[Client]()

  /**
   * Adds a client, removes a client or sends a message to all the clients.
   */
  def receive = {

    case AddClient(user, actor) => {
      System.out.println("Added client " + user.name)

      val newLogin = Json.obj(
        "type" -> "login",
        "user" -> user.name
      )
      clients.foreach( client => {
        //sends the new login message to the rest of clients
        client.actor ! newLogin

        //notifies to the new client all the current clients connected
        val currentLogin = Json.obj(
          "type" -> "login",
          "user" -> client.userName
        )
        actor ! currentLogin
      })

      clients.enqueue(Client(user.name, actor))
    }

    case RemoveClient(user, actor) => {
      val clientToRemove = Client(user.name, actor)

      if(clients.dequeueFirst(_ == clientToRemove).isDefined) {
        System.out.println("Removed client " + user.name)

        //sends the logout message to the rest of clients
        val logout = Json.obj(
          "type" -> "logout",
          "user" -> user.name
        )
        clients.foreach(client => client.actor ! logout);
      } else {
        System.out.println("Trying to remove client " + user.name + " but it doesn't exist!")
      }
    }

    case MessageAll(m) => {
      System.out.println("Message from " + m.user.name + ": " + m.text)

      val json = Json.obj(
        "type" -> "message",
        "message" -> m.text,
        "user" -> m.user.name,
        "color" -> m.user.color
      )
      clients.foreach(client => client.actor ! json)
    }
  }
}