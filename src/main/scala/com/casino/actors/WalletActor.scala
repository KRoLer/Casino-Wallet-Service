package com.casino.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import com.casino.actors.WalletActor._

import scala.collection.mutable

class WalletActor extends PersistentActor with ActorLogging {

  override def persistenceId: String = "wallet-persistent-actor"

  private var state: mutable.HashMap[Long, Double] = mutable.HashMap.empty

  def updateState(evt: Event) = evt match {
    case Registered(id, balance) => state.put(id, balance)
    case Deposited(id, balance) => state.update(id, balance)
    case Withdrawn(id, balance) => state.update(id, balance)
  }

  override def receiveCommand: Receive = {
    case Register(id) if state.contains(id) => {
      val msg = "User already registered!"
      log.info(msg)
      sender() ! Left(msg)
    }
    case Register(id) => {
      log.info(s"User with id: $id has been registered!")

      val requester = sender
      persist(Registered(id, 0.0))(evt => {
        updateState(evt)
        requester ! Right(state(id))
      })
    }
    case Balance(id) if state.contains(id) => {
      log.info(s"Balance for user: $id is ${state(id)}")

      sender() ! Right(state(id))
    }
    case Deposit(id, amount) if state.contains(id) => {
      log.info(s"Deposit for user: $id with amount $amount")
      val newBalance = state(id) + amount

      val requester = sender
      persist(Deposited(id, newBalance))(evt => {
        updateState(evt)
        requester ! Right(newBalance)
      })
    }
    case Withdraw(id, amount) if state.contains(id) => {
      val balance = state(id)
      val newBalance = balance - amount

      val requester = sender
      if (newBalance >= 0) {
        log.info(s"User $id withdraw $amount and new balance is: $newBalance")
        persist(Withdrawn(id, newBalance))(evt => {
          updateState(evt)
          requester ! Right(newBalance)
        })
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

  override def receiveRecover: Receive = {
    case evt: Event => updateState(evt); log.info(s"Recovered $evt")
    case SnapshotOffer(_, snapshot: mutable.HashMap[Long, Double]) => state = snapshot
  }
}

object WalletActor {
  def props = Props[WalletActor]

  case class Register(id: Long)
  case class Balance(id: Long)
  case class Deposit(id: Long, amount: Double)
  case class Withdraw(id: Long, amount: Double)

  sealed trait Event
  case class Registered(id: Long, balance: Double) extends Event
  case class Deposited(id: Long, balance: Double) extends Event
  case class Withdrawn(id: Long, balance: Double) extends Event


}