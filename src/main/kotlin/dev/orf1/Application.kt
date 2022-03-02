package dev.orf1


fun main() {
    var selection = ""
    while(selection != "1" && selection != "2" && selection != "3") {
        if (selection != "") {
            println("Invalid option. Please type either \"1\", \"2\", or \"3\".")
        }
        print("Please choose one of the following options: \n1 - Client\n2 - Server\n3 - Exit\n-> ")
        selection = readLine().toString()
    }

    when (selection) {
        "1" -> {
            println("Starting Cobalt Client.")
            runClient()
        }
        "2" -> {
            println("Starting Cobalt Server.")
            runServer()
        }
        "3" -> {
            println("Program exiting.")
        }
    }
}

fun runServer() {
    Server("127.0.0.1", 8080)
}

fun runClient() {
    Client("127.0.0.1", 8080)
}