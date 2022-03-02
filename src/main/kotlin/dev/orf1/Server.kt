package dev.orf1

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


internal class Server(host: String, port: Int) {
    private val server: NettyApplicationEngine
    private val registered: ConcurrentMap<String, EncryptedProfile> = ConcurrentHashMap()

    init {
        server = embeddedServer(Netty, port = port, host = host) {
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }

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

                post("/login") {
                    val incoming = call.receive<LoginRequest>()
                    println("Received login request: $incoming")
                    val outgoing = Profile(incoming.email, incoming.password, "Admin", "12tx7182b9")
                    println("Responding with: $outgoing")
                    call.respond(outgoing)
                }

                post("/register") {
                    val packet = call.receive<RegisterRequest>()
                    if (registered.containsKey(packet.email)) {
                        call.respond(HttpStatusCode.Unauthorized)
                    }

                    //TODO Hash password, add username verification.
                    val profile = EncryptedProfile(packet.email, packet.password, packet.username,"16n492167")

                    registered[profile.email] = profile

                }
            }
        }
        start()
    }

    private fun start() {
        server.start(wait = true)
    }


}