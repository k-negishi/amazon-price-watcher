package com.acme.amazonpricewatcher.function

import com.acme.amazonpricewatcher.usecase.Orchestrate
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.function.Supplier

@Component("amazonPriceWatcher")
class AmazonPriceWatcherSupplier(
    private val orchestrate: Orchestrate
) : Supplier<Mono<String>> {
    override fun get(): Mono<String> = mono {
        orchestrate.execute()
        "OK"
    }
}
