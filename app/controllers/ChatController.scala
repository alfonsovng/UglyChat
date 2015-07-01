package controllers

import models.User
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api._
import play.api.mvc.{Session => _, _}
import play.api.Play.current

object ChatController extends Controller {

  /**
   * Main route. Redirects to the login page
   */
  def index = Action {
    Redirect(routes.ChatController.prepareLogin)
  }

  val LoginForm = Form(
    mapping (
      "name" -> nonEmptyText,
      "color" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  /**
   * Login page
   */
  def prepareLogin = Action {
    Ok(views.html.login(LoginForm))
  }

  /**
   * If login is ok, redirects to the chat page
   */
  def login = Action { implicit request =>
    LoginForm.bindFromRequest.fold(
      errors => BadRequest(views.html.login(errors)),
      user => Redirect(routes.ChatController.chat(user))
    )
  }

  /**
   * Chat page
   */
  def chat(user:User) = Action {
    Ok(views.html.chat(user))
  }

  /**
   * Websocket entry point using actors
   */
  def websocket(user:User) = WebSocket.acceptWithActor[JsValue, JsValue] {
    request => out =>
      ChatActor.props(user, out)
  }
}
