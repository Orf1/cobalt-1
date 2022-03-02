package dev.orf1

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.util.*


data class Profile(val email: String, val password: String, val username: String, val token: String)

internal class Client(private val host: String, private val port: Int) {
    private lateinit var client: HttpClient
    private var connected: Boolean = false
    private var authenticated: Boolean = false
    private lateinit var profile: Profile

    init {
        println("Starting setup stage.")
        setup()
        println("Starting authentication stage.")
        authenticate()
        println("Starting onnection stage.")
        connect()
    }

    private fun setup() {
        client = HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    private fun connect() {
        runBlocking {
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



    private fun authenticate() {
        var selection = ""
        while(selection != "1" && selection != "2") {
            if (selection != "") {
                println("Invalid option. Please type either \"1\" or \"2\".")
            }
            print("How would you like to authenticate?\n1 - Login\n2 - Register\n-> ")
             selection = readLine().toString()
        }

        when(selection) {
            "1" -> {
                login()
            }

            "2" -> {
                register()
            }
        }
    }

    private fun login() {
        runBlocking {
            while (!authenticated) {
                print("Please enter your email: -> ")
                val email = readLine()
                print("Please enter your password: -> ")
                val password = readLine()
                if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                    try {
                        val response: HttpResponse = client.post("http://127.0.0.1:8080/login") {
                            contentType(ContentType.Application.Json)
                            setBody(LoginRequest(email, password))
                        }
                        if(response.status == HttpStatusCode.OK) {
                            val packet = response.body<AuthenticationResponse>()
                            profile = Profile(email, password, packet.username, packet.token)
                            authenticated = true
                        } else {
                            println("Something went wrong, please try again.")
                        }
                    } catch (e:ClientRequestException) {
                        println("Invalid username or password, please try again.")
                        e.printStackTrace()
                    } catch (e: ConnectException) {
                        println("Server unavailable, please try again.")
                    } catch (e: Throwable) {
                        println("Something went wrong, please try again.")
                        e.printStackTrace()
                    }
                } else {
                    println("Email or password cannot be empty.")
                }
            }
        }
    }

    private fun register() {

    }
}