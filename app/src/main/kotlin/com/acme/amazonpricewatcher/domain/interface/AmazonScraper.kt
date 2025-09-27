package com.acme.amazonpricewatcher.domain.`interface`

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import java.time.LocalDate

interface AmazonScraper {
    suspend fun scrape(
        item: AmazonItemProperties.ItemConfig,
        targetDate: LocalDate
    ): PriceHistory
}