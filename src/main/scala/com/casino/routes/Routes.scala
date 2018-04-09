package com.casino.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.casino.service.WalletService
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success}

case class Balance(balance: Double)
case class User(playerId: Long, amount: Option[Double])
case class GeneralError(msg: String = "Internal server error.")

trait Protocols extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val balanceFormat = jsonFormat1(Balance.apply)
  implicit val userFormat = jsonFormat2(User.apply)
  implicit val generalErrorFormat = jsonFormat1(GeneralError.apply)
}

class UnsecuredRoutes (service: WalletService) extends Protocols {
  import service.{BalanceError, UserError}

  def routes: Route = logRequestResult("casino-wallet-microservice") {
    pathPrefix("api" / "v1") {
      path("balance" / LongNumber) { playerId =>
        get {
          onComplete(service.balance(playerId)) {
            case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
            case Success(Left(UserError(msg))) => complete(StatusCodes.BadRequest -> GeneralError(msg))

            case Success(_) => complete(StatusCodes.InternalServerError -> GeneralError())
            case Failure(error) => complete(StatusCodes.InternalServerError -> GeneralError(error.getMessage))
          }
        }
      } ~
        path("register") {
          (post & entity(as[User])) { user =>
            onComplete(service.register(user.playerId)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(UserError(msg))) => complete(StatusCodes.BadRequest -> GeneralError(msg))

              case Success(_) => complete(StatusCodes.InternalServerError -> GeneralError())
              case Failure(error) => complete(StatusCodes.InternalServerError -> GeneralError(error.getMessage))
            }
          }
        } ~
        path("deposit") {
          (post & entity(as[User])) { user =>
            onComplete(service.deposit(user.playerId, user.amount)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(UserError(msg))) => complete(StatusCodes.BadRequest -> GeneralError(msg))

              case Success(_) => complete(StatusCodes.InternalServerError -> GeneralError())
              case Failure(error:IllegalArgumentException) => complete(StatusCodes.BadRequest -> GeneralError(error.getMessage))
              case Failure(error) => complete(StatusCodes.InternalServerError -> GeneralError(error.getMessage))
            }
          }
        } ~
        path("withdraw") {
          (post & entity(as[User])) { user =>
            onComplete(service.withdraw(user.playerId, user.amount)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(BalanceError(_, Some(balance)))) => complete(StatusCodes.BadRequest -> Balance(balance))
              case Success(Left(UserError(msg))) => complete(StatusCodes.BadRequest -> GeneralError(msg))
              case Failure(error:IllegalArgumentException) => complete(StatusCodes.BadRequest -> GeneralError(error.getMessage))

              case Success(_) => complete(StatusCodes.InternalServerError -> GeneralError())
              case Failure(error) => complete(StatusCodes.InternalServerError -> GeneralError(error.getMessage))
            }
          }
        }
    }
  }

}
