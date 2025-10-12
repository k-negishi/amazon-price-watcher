package com.acme.pricechecker.plugins

import com.acme.pricechecker.lambda.fetchprice.fetchPriceModule
import com.acme.pricechecker.lambda.getpricehistory.getPriceHistoryModule
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDI() {
    install(Koin) {
        // Logging is removed for now to solve the compilation issue.
        modules(appModule)
    }
}

val appModule = module {
    // Import modules from each lambda
    includes(fetchPriceModule, getPriceHistoryModule)
}
