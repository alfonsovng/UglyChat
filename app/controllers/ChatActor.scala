package controllers

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import models.{User,Message}
import scala.collection.mutable

/**
 * Every instance of ChatActor represents a client.
 */
class ChatActor(user:User, client: ActorRef, clientListActor:ActorRef) extends Actor {

  /**
   * Adds the client to the client list
   */
  override def preStart() = {
    clientListActor ! AddClient(client)
  }

  /**
   * Removes the client from the client list
   */
  override def postStop() = {
    clientListActor ! RemoveClient(client)
  }

  /**
   * Propagates the message received to all the clients
   * using the ClientListActor
   */
  def receive = {
    case s: String => clientListActor ! MessageAll(Message(user,s))
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

case class AddClient(val client:ActorRef) extends ClientListActorMsg

case class RemoveClient(val client:ActorRef) extends ClientListActorMsg

case class MessageAll(val message:Message) extends ClientListActorMsg

/**
 * This actor manages the list of clients. It's possible to add a client,
 * remove a client or send a message to all the clients.
 */
class ClientListActor extends Actor {

  /**
   * List (well, queue) of clients
   */
  private val clients = mutable.Queue[ActorRef]()

  /**
   * Adds a client, removes a client or sends a message to all the clients.
   */
  def receive = {
    case AddClient(c) => clients.enqueue(c)
    case RemoveClient(c) => clients.dequeueFirst(_ == c)
    case MessageAll(m) => clients.foreach(c => c ! m.text)
  }
}