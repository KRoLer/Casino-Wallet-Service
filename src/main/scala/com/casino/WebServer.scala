package com.casino


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{HttpApp, Route}
import com.casino.service.WalletService
import spray.json.DefaultJsonProtocol

import scala.util.{Failure, Success}

case class Balance(balance: Double)

case class User(playerId: Long, amount: Option[Double])


trait Protocols extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val balanceFormat = jsonFormat1(Balance.apply)
  implicit val userFormat = jsonFormat2(User)
}

object WebServer extends HttpApp with Protocols {

  override protected def routes: Route = logRequestResult("casino-wallet-microservice") {
    pathPrefix("api" / "v1") {
      path("balance" / LongNumber) { playerId =>
        get {
          onComplete(WalletService.balance(playerId)) {
            case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
            case Success(Left(error)) => complete(StatusCodes.BadRequest -> error)
            case Failure(error) => complete(StatusCodes.InternalServerError -> error)
          }
        }
      } ~
        path("register") {
          (post & entity(as[User])) { user =>
            onComplete(WalletService.register(user.playerId)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(error)) => complete(StatusCodes.BadRequest -> error)
              case Failure(error) => complete(StatusCodes.InternalServerError -> error)
            }
          }
        } ~
        path("deposit") {
          (post & entity(as[User])) { user =>
            onComplete(WalletService.deposit(user.playerId, user.amount)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(error)) => complete(StatusCodes.BadRequest -> error)
              case Failure(error) => complete(StatusCodes.InternalServerError -> error)
            }
          }
        } ~
        path("withdraw") {
          (post & entity(as[User])) { user =>
            onComplete(WalletService.withdraw(user.playerId, user.amount)) {
              case Success(Right(balance)) => complete(StatusCodes.OK -> Balance(balance))
              case Success(Left(balance)) => complete(StatusCodes.BadRequest -> Balance(balance))
              case Failure(error) => complete(StatusCodes.InternalServerError -> error)
            }
          }
        }
    }
  }
}

