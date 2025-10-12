package com.acme.pricechecker.lambda.fetchprice.handler

import com.acme.pricechecker.lambda.fetchprice.usecase.FetchPriceUsecase
import com.acme.pricechecker.plugins.configureDI
import com.acme.pricechecker.plugins.configureSerialization
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

class Handler : RequestStreamHandler {
    private val ktorEngine: NettyApplicationEngine

    init {
        ktorEngine = embeddedServer(Netty, port = 8080, module = Application::module)
        ktorEngine.start()
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = runBlocking {
        val usecase by ktorEngine.application.inject<FetchPriceUsecase>()
        val requestBody = input.bufferedReader().use { it.readText() }
        val request = Json.decodeFromString<FetchPriceRequest>(requestBody)

        val results = usecase.execute(request.targetDate)

        output.write("""{"status": "success", "itemCount": ${results.size}}""".toByteArray())
    }
}

// Moved Ktor module configuration to a top-level function
fun Application.module() {
    configureDI()
    configureSerialization()
}

@Serializable
data class FetchPriceRequest(
    val date: String
) {
    val targetDate: LocalDate by lazy { LocalDate.parse(date) }
}
