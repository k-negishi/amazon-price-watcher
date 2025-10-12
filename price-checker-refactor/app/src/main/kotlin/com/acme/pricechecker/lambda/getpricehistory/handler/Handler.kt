package com.acme.pricechecker.lambda.getpricehistory.handler

import com.acme.pricechecker.lambda.fetchprice.usecase.ItemConfig
import com.acme.pricechecker.lambda.getpricehistory.usecase.GetPriceHistoryUsecase
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
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler

class Handler : RequestStreamHandler {
    private val ktorEngine: NettyApplicationEngine

    init {
        ktorEngine = embeddedServer(Netty, port = 8080, module = Application::module)
        ktorEngine.start()
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = runBlocking {
        val usecase by ktorEngine.application.inject<GetPriceHistoryUsecase>()
        val requestBody = input.bufferedReader().use { it.readText() }
        val request = Json.decodeFromString<GetPriceHistoryRequest>(requestBody)

        val results = usecase.execute(request.items, request.targetDate)

        val responseJson = Json.encodeToString(results)
        output.write(responseJson.toByteArray())
    }
}

fun Application.module() {
    com.acme.pricechecker.plugins.configureDI()
    com.acme.pricechecker.plugins.configureSerialization()
}

@Serializable
data class GetPriceHistoryRequest(
    val items: List<ItemConfig>,
    val date: String
) {
    val targetDate: LocalDate by lazy { LocalDate.parse(date) }
}
