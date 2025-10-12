package com.acme.pricechecker.lambda.fetchprice.usecase

import com.acme.pricechecker.domain.api.AmazonScraper
import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.domain.repository.PriceHistoryRepository
import com.acme.pricechecker.domain.vo.URL
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDate

class FetchPriceUsecase(
    private val scraper: AmazonScraper,
    private val repository: PriceHistoryRepository
) {
    suspend fun execute(targetDate: LocalDate): List<PriceHistory> = coroutineScope {
        // Dummy item list for now. This should be injected from an external configuration.
        val items = listOf(
            ItemConfig(name = "Sample Item 1", url = URL("https://www.amazon.co.jp/dp/B000000000")),
            ItemConfig(name = "Sample Item 2", url = URL("https://www.amazon.co.jp/dp/B000000001"))
        )

        val semaphore = Semaphore(permits = 3)

        val results = items.map { item ->
            async {
                semaphore.withPermit {
                    val result = scraper.scrape(item, targetDate)
                    repository.save(result)
                    result
                }
            }
        }

        results.awaitAll()
    }
}

// A simple data class to hold item configuration
data class ItemConfig(
    val name: String,
    val url: URL
)
