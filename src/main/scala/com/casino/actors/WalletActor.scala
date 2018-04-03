package com.casino.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.casino.actors.WalletActor._

import scala.collection.mutable

class WalletActor extends Actor with ActorLogging {

  private val state = mutable.HashMap[Long, Double]()

  override def receive: Receive = {
    case Register(id) if state.contains(id) => {
      val msg = "User already registered!"
      log.info(msg)
      sender() ! Left(msg)
    }
    case Register(id) => {
      log.info(s"User with id: $id has been registered!")

      state.put(id, 0.0)
      sender() ! Right(state(id))
    }
    case Balance(id) if state.contains(id) => {
      log.info(s"Balance for user: $id is ${state(id)}")

      sender() ! Right(state(id))
    }
    case Deposit(id, amount) if state.contains(id) => {
      log.info(s"Deposit for user: $id with amount $amount")
      val newBalance = state(id) + amount
      state += id -> newBalance

      sender() ! Right(newBalance)
    }
    case Withdraw(id, amount) if state.contains(id) => {
      val balance = state(id)
      val newBalance = balance - amount

      if (newBalance >= 0) {
        log.info(s"User $id withdraw $amount and new balance is: $newBalance")
        state += id -> newBalance
        sender ! Right(newBalance)
      } else {
        log.info(s"User $id try withdraw $amount. Insufficient balance: $balance")
        sender ! Left(("Insufficient balance", balance))
      }
    }
    case _ => {
      val msg = "User not found!"
      log.info(msg)
      sender() ! Left(msg)
    }
  }
}

object WalletActor {
  def props = Props[WalletActor]

  case class Register(id: Long)

  case class Balance(id: Long)

  case class Deposit(id: Long, amount: Double)

  case class Withdraw(id: Long, amount: Double)

}