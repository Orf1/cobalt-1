package dev.orf1

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.net.ConnectException
import java.util.*


internal class Client(private val host: String, private val port: Int) {
    private val client: HttpClient
    private var connected: Boolean = false

    init {
        println("Client configured to connect to $host:$port")
        client = HttpClient(CIO) {
            install(WebSockets)
        }
        runBlocking {
            connect()
        }
    }

    private suspend fun connect() {
        while (true) {
            println("Trying to connect to server.")
            try {
                client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/chat") {
                    connected = true
                    println("Established connection to server.")
                    while (true) {
                        val othersMessage = incoming.receive() as? Frame.Text
                        println("[Server] " + othersMessage?.readText())
                        val myMessage = Scanner(System.`in`).nextLine()
                        if (myMessage != null) {
                            send(myMessage)
                        }
                    }
                }
                client.close()
            } catch (e: ConnectException) {
                connected = false
                println("Unable to connect to server, retrying in 5 seconds.")
                delay(5000)
            } catch (e: ClosedReceiveChannelException) {
                connected = false
                println("Server closed connection, trying to reconnect in 5 seconds.")
                delay(5000)
            } catch (e: Throwable) {
                connected = false
                println("Disconnected due to an error, trying to reconnect in 5 seconds.")
                delay(5000)
            }
        }
    }
}