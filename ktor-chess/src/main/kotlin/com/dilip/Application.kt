package com.dilip

import com.dilip.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureMonitoring()
    configureRouting()
}
