package dev.orf1

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.net.ConnectException
import java.util.*
import javax.naming.AuthenticationException


internal class Client(private val host: String, private val port: Int) {
    private lateinit var client: HttpClient
    private var connected: Boolean = false
    private var authenticated: Boolean = false
    private lateinit var profile: Profile
    private val encryptionManager = EncryptionManager()

    init {
        println("Starting setup stage.")
        setup()
        println("Starting authentication stage.")
        //authenticate()
        println("Starting connection stage.")
        connect()
    }

    private fun setup() {
        client = HttpClient(CIO) {

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)

            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = false
                })
            }
            install(Auth) {
                digest {
                    credentials {
                        DigestAuthCredentials(username = "username", password = "password")
                    }
                    realm = "Access to the '/socket' path"
                }
            }
        }
    }

    private fun connect() {
//        if (!authenticated) {
//            authenticate()
//        }

        val auth = client.plugin(Auth)
        auth.providers.removeAt(0)
        auth.digest {
            credentials {
                DigestAuthCredentials(username = "jetbrains", password = "foobar")
            }
            realm = "Access to the '/socket' path"
        }

        runBlocking {
            while (true) {
                println("Trying to connect to server.")
                try {
                    client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/socket") {
                        connected = true
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
                } catch(e: ClientRequestException) {
                    connected = false
                    println("Authentication failed, retrying in 5 seconds.")
                    delay(5000)
                } catch (e: ClosedReceiveChannelException) {
                    connected = false
                    println("Server closed connection, trying to reconnect in 5 seconds.")
                        delay(5000)
                } catch (e: Throwable) {
                    connected = false
                    println("Disconnected due to an error, trying to reconnect in 5 seconds.")
                    e.printStackTrace()
                    delay(5000)
                }
            }
        }
    }


    private fun authenticate() {
        runBlocking {
            while (!authenticated) {
                var selection = ""
                while (selection != "1" && selection != "2") {
                    if (selection != "") {
                        println("Invalid option. Please type either \"1\" or \"2\".")
                    }
                    print("How would you like to authenticate?\n1 - Login\n2 - Register\n-> ")
                    selection = readLine().toString()
                }
                when (selection) {
                    "1" -> {
                        login()
                    }
                    "2" -> {
                        register()
                    }
                }
            }
        }
    }

    private suspend fun login() {
            print("Please enter your email: -> ")
            val email = readLine()
            print("Please enter your password: -> ")
            val password = readLine()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                println("Email or password can not be empty!")
                return
            }

            try {
                val response: HttpResponse = client.post("http://$host:$port/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, password))
                }
                if (response.status == HttpStatusCode.OK) {
                    val authResponse = response.body<AuthResponse>()
                    profile = Profile(email, encryptionManager.hash(password), authResponse.username, authResponse.token)
                    authenticated = true
                    println("AuthResponse received from server: $authResponse")
                    println("Successfully logged in.")
                } else {
                    println("Something went wrong, please try again.")
                }
            } catch (e: ClientRequestException) {
                println("Invalid username or password, please try again.")
            } catch (e: ConnectException) {
                println("Server unavailable, please try again.")
            } catch (e: Throwable) {
                println("Something went wrong, please try again.")
                e.printStackTrace()
            }
    }

    private suspend fun register() {
        print("Please enter your email: -> ")
        val email = readLine()
        print("Please enter your password: -> ")
        val password = readLine()
        print("Please enter your username: -> ")
        val username = readLine()

        if (email.isNullOrEmpty() || password.isNullOrEmpty() || username.isNullOrEmpty()) {
            println("Email, password or username can not be empty!")
            return
        }

        try {
            val response: HttpResponse = client.post("http://$host:$port/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password, username))
            }
            if (response.status == HttpStatusCode.OK) {
                val authResponse = response.body<AuthResponse>()
                profile = Profile(email, encryptionManager.hash(password), authResponse.username, authResponse.token)
                authenticated = true
                println("AuthResponse received from server: $authResponse")
                println("Successfully registered.")
            } else {
                println("Something went wrong, please try again.")
            }
        } catch (e: ClientRequestException) {
            println("Email or username already registered.")
        } catch (e: ConnectException) {
            println("Server unavailable, please try again.")
        } catch (e: Throwable) {
            println("Something went wrong, please try again.")
            e.printStackTrace()
        }
    }
}