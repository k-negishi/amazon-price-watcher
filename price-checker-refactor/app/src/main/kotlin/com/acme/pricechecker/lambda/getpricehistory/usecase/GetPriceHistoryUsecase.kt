package com.acme.pricechecker.lambda.getpricehistory.usecase

import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.domain.repository.PriceHistoryRepository
import com.acme.pricechecker.lambda.fetchprice.usecase.ItemConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class GetPriceHistoryUsecase(
    private val repository: PriceHistoryRepository
) {
    suspend fun execute(items: List<ItemConfig>, targetDate: LocalDate): List<PriceHistory> = coroutineScope {
        val results = items.map { item ->
            async {
                repository.findById(item.url, targetDate)
            }
        }
        results.awaitAll().filterNotNull()
    }
}
