package dev.orf1

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException


internal class Server(host: String, port: Int) {
    private val server: NettyApplicationEngine

    init {
        server = embeddedServer(Netty, port = port, host = host) {
            install(WebSockets)
            routing {
                webSocket("/chat") {
                    println("New websocket connection.")
                    outgoing.send(Frame.Text("Welcome!"))
                    try {
                        for (frame in incoming) {
                            val text = (frame as Frame.Text).readText()
                            println("Received text from client: $text")
                            outgoing.send(Frame.Text(text))
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        println("Channel closed. Reason: ${closeReason.await()}")
                    } catch (e: Throwable) {
                        println("Channel closed due to an error. Reason: ${closeReason.await()}")
                        e.printStackTrace()
                    }
                }
            }
        }
        start()
    }

    private fun start() {
        server.start(wait = true)
    }
}