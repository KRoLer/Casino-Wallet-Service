package com.casino.service

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.casino.actors.WalletActor._

import scala.concurrent.Future
import scala.concurrent.duration._




trait WalletService {
  def register(id: Long): Future[Either[Error, Double]]
  def balance(id: Long): Future[Either[Error, Double]]
  def deposit(playerId: Long, balance: Option[Double]): Future[Either[Error, Double]]
  def withdraw(playerId: Long, balance: Option[Double]): Future[Either[Error, Double]]

  sealed trait Error
  case class UserError(msg:String) extends Error
  case class GeneralError(msg:String) extends Error
  case class BalanceError(msg:Option[String], balance: Option[Double]) extends Error

  protected def toError(msg: Either[Any, Double]): Either[Error, Double] = msg match {
    case Right(value) => Right(value)
    case Left(msg:String) => Left(UserError(msg))
    case Left((msg:String, balance: Double)) => Left(BalanceError(Option(msg), Option(balance)))
    case _ => Left(GeneralError("Unknown Error"))
  }
}

class ActorWalletService (wallet:ActorRef) extends WalletService {
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(5 seconds)

  def register(id: Long): Future[Either[Error, Double]] =
    (wallet ? Register(id)).mapTo[Either[Error, Double]].map(toError(_))


  def balance(id: Long): Future[Either[Error, Double]] =
    (wallet ? Balance(id)).mapTo[Either[Error, Double]].map(toError(_))

  def deposit(playerId: Long, balance: Option[Double]): Future[Either[Error, Double]] = balance match {
    case Some(balanceVal) if balanceVal >= 0 => (wallet ? Deposit(playerId, balanceVal)).mapTo[Either[Error, Double]].map(toError(_))
    case Some(balanceVal) if balanceVal < 0 => Future.failed(new IllegalArgumentException("Amount should be greater than 0"))
    case None => Future.failed(new IllegalArgumentException("Amount can't be empty"))
  }

  def withdraw(playerId: Long, balance: Option[Double]): Future[Either[Error, Double]] = balance match {
    case Some(balanceVal) if balanceVal >= 0 => (wallet ? Withdraw(playerId, balanceVal)).mapTo[Either[Error, Double]].map(toError(_))
    case Some(balanceVal) if balanceVal < 0 => Future.failed(new IllegalArgumentException("Amount should be greater than 0"))
    case None => Future.failed(new IllegalArgumentException("Amount can't be empty"))
  }
}
