package com.casino.service

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.casino.actors.WalletActor
import com.casino.actors.WalletActor._

import scala.concurrent.Future
import scala.concurrent.duration._


object WalletService {

  val system: ActorSystem = ActorSystem("wallet-system")
  val wallet: ActorRef = system.actorOf(WalletActor.props)
  implicit val timeout = Timeout(5 seconds)

  def register(id: Long): Future[Either[String, Double]] =
    (wallet ? Register(id)).mapTo[Either[String, Double]]


  def balance(id: Long): Future[Either[String, Double]] =
    (wallet ? Balance(id)).mapTo[Either[String, Double]]

  def deposit(playerId: Long, balance: Option[Double]): Future[Either[String, Double]] = balance match {
    case Some(balanceVal) if balanceVal > 0 => (wallet ? Deposit(playerId, balanceVal)).mapTo[Either[String, Double]]
    case None => Future.successful(Left("Balance value empty or less than 0."))
  }

  def withdraw(playerId: Long, balance: Option[Double]): Future[Either[Double, Double]] = balance match {
    case Some(balanceVal) if balanceVal > 0 => (wallet ? Withdraw(playerId, balanceVal)).mapTo[Either[Double, Double]]
    case None => Future.successful(Left(-1))
  }


}
