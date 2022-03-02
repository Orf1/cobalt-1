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
import javax.naming.AuthenticationException


internal class Server(host: String, port: Int) {
    private val server: NettyApplicationEngine
    private val registered: ConcurrentMap<String, EncryptedProfile> = ConcurrentHashMap()
    private val encryptionManager = EncryptionManager()

    init {
        server = embeddedServer(Netty, port = port, host = host) {
            install()
            setupRouting()
        }
        server.start(wait = true)
    }

    private fun Application.install() {
        install(WebSockets)
        install(ContentNegotiation) { json() }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                if(cause is AuthenticationException) {
                    log.debug("Authentication exception occurred. Responding with 401.")
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    log.debug("Exception occurred. Responding with 500.")
                    call.respond(HttpStatusCode.InternalServerError)
                    cause.printStackTrace()
                }
            }
        }

    }

    private fun Application.setupRouting() {
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
                val request = call.receive<LoginRequest>()
                log.debug("Received login request: $request")

                val profile = registered[request.email] ?: throw AuthenticationException("Invalid email or password.")

                if (profile.hashedPassword != encryptionManager.hash(request.password)) {
                    throw AuthenticationException("Invalid email or password.")
                }

                profile.token = encryptionManager.generateToken()
                val response = AuthResponse(profile.username, profile.token)

                log.debug("Responding with: $response")
                log.debug("Account successfully logged in. Profile: $profile")
                call.respond(response)
            }

            post("/register") {
                val request = call.receive<RegisterRequest>()
                log.debug("Received register request: $request")

                if (registered.containsKey(request.email)) {
                    throw AuthenticationException("Email or username unavailable.")
                }

                registered.forEach {
                    if (it.value.username == request.username) {
                        throw AuthenticationException("Email or username unavailable.")
                    }
                }

                val token = encryptionManager.generateToken()
                val profile = EncryptedProfile(request.email, encryptionManager.hash(request.password), request.username, token)
                registered[profile.email] = profile
                val response = AuthResponse(profile.username, profile.token)

                log.debug("Responding with: $response")
                log.debug("Account successfully registered. Profile: $profile")
                call.respond(response)
            }
        }
    }
}