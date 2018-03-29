package com.casino

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.casino.actors.WalletActor
import com.casino.routes.UnsecuredRoutes
import com.casino.service.ActorWalletService
import com.typesafe.config.ConfigFactory

object WalletWebservice extends App{

  implicit val system = ActorSystem("wallet-service-system")
  implicit val materializer = ActorMaterializer()
  implicit val walletActor = system.actorOf(WalletActor.props, "wallet-actor")

  val walletService = new ActorWalletService(walletActor)
  val routes = new UnsecuredRoutes(walletService).routes

  val conf = ConfigFactory.load()

  Http().bindAndHandle(routes, conf.getString("service.host"), conf.getInt("service.port"))
}
