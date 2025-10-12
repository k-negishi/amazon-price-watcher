package com.acme.pricechecker

import com.acme.pricechecker.plugins.configureDI
import com.acme.pricechecker.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDI()
    configureSerialization()
    // Other plugins like routing, security, etc. will be added later
}
