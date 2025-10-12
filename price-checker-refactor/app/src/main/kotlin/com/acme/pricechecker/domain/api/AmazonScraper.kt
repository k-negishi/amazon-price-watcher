package com.acme.pricechecker.domain.api

import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.lambda.fetchprice.usecase.ItemConfig
import java.time.LocalDate

interface AmazonScraper {
    suspend fun scrape(
        item: ItemConfig,
        targetDate: LocalDate
    ): PriceHistory
}
