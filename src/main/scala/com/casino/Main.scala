package com.casino

import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {

    WebServer.startServer("localhost", 8080)
  }
}
