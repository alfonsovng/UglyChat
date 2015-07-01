package controllers

import akka.actor.{ActorSystem, Actor, ActorRef, Props}
import scala.collection.mutable

class ChatActor(client: ActorRef, clientListActor:ActorRef) extends Actor {

  override def preStart() = {
    clientListActor ! AddClient(client)
  }

  override def postStop() = {
    clientListActor ! RemoveClient(client)
  }

  def receive = {
    case s: String => clientListActor ! MessageAll(s)
  }
}

object ChatActor {

  val clientListActor = ActorSystem().actorOf(Props[ClientListActor])

  def props(outChannel: ActorRef) = {
    Props(new ChatActor(outChannel, clientListActor))
  }
}

sealed trait ClientListActorMsg{}

case class AddClient(val client:ActorRef) extends ClientListActorMsg

case class RemoveClient(val client:ActorRef) extends ClientListActorMsg

case class MessageAll(val message:String) extends ClientListActorMsg

class ClientListActor extends Actor {

  private val clients = mutable.Queue[ActorRef]()

  def receive = {
    case AddClient(c) => clients.enqueue(c)
    case RemoveClient(c) => clients.dequeueFirst(_ == c)
    case MessageAll(m) => clients.foreach(c => c ! m)
  }
}