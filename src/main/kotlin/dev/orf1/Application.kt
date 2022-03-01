package dev.orf1


fun main() {
    println("Please choose one of the following options: \n1 - Client\n2 - Server\n3 - Exit")
    when (readLine()) {
        "1" -> {
            println("Starting Cobalt Client.")
            runClient()
        }
        "2" -> {
            println("Starting Cobalt Server.")
            runServer()
        }
        else -> {
            println("Selected mode is not yet supported.")
        }
    }
    println("Program exiting.")
}

fun runServer() {
    Server("127.0.0.1", 8080)
}

fun runClient() {
    Client("127.0.0.1", 8080)
}