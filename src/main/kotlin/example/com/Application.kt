package example.com

import example.com.plugins.*
import example.com.routing.configurePetRoutes
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
//    configureDatabases()
    configurePetRoutes()
    configureRouting()
}
