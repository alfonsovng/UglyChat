package controllers

import models.User
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api._
import play.api.mvc.{Session => _, _}
import play.api.Play.current

object ChatController extends Controller {

  def index = Action {
    Redirect(routes.ChatController.prepareLogin)
  }

  val LoginForm = Form(
    mapping (
      "name" -> nonEmptyText,
      "color" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def prepareLogin = Action {
    Ok(views.html.login(LoginForm))
  }

  def login = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      errors => BadRequest(views.html.login(errors)),
      user => Redirect(routes.ChatController.chat(user))
    )
  }

  def chat(user:User) = Action {
    Ok(views.html.chat(user))
  }

  def websocket = WebSocket.acceptWithActor[String, String] {
    request => out =>
      ChatActor.props(out)
  }
}
