package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.api.AmazonScraper
import com.acme.amazonpricewatcher.domain.repository.PriceHistoryRepository
import com.acme.amazonpricewatcher.fw.logger
import java.time.LocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service

@Service
class FetchAmazonPriceUsecase(
    private val properties: AmazonItemProperties,
    private val scraper: AmazonScraper,
    private val repository: PriceHistoryRepository
) {
    suspend fun execute(
        targetDate: LocalDate
    ): List<PriceHistory> = coroutineScope {
        // 並列数を制限するセマフォ
        val semaphore = Semaphore(permits = 3)

        val results = properties.items.map { item ->
            async {
                semaphore.withPermit {
                    try {
                        // スクレイピングの実行
                        val result = scraper.scrape(item, targetDate)

                        // 保存
                        repository.save(result)

                        return@async result
                    } catch (ex: Exception) {
                        logger.warn("スクレイピングに失敗しました url={} date={}", item.url, targetDate, ex)
                        return@async null
                    }
                }
            }
        }

        return@coroutineScope results.awaitAll().filterNotNull()
    }
}
