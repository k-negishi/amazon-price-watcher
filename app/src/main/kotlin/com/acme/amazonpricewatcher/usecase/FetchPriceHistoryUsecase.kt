package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.vo.URL
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.repository.PriceHistoryRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate

class FetchPriceHistoryUsecase(
    private val properties: AmazonItemProperties,
    private val repository: PriceHistoryRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun execute(
        targetDate: LocalDate
    ): List<PriceHistory> {
        val snapshots = mutableListOf<PriceHistory>()

        for (item in properties.items) {
            val url = URL.from(item.url)

            try {
                val snapshot = repository.findById(url = url, date = targetDate)
                if (snapshot != null) {
                    snapshots.add(snapshot)
                    logger.debug("価格履歴を取得しました url={} date={}", item.url, targetDate)
                } else {
                    logger.debug("価格履歴が見つかりませんでした url={} date={}", item.url, targetDate)
                }
            } catch (e: Exception) {
                logger.warn("価格履歴の取得に失敗しました url={} date={}", item.url, targetDate, e)
            }
        }

        return snapshots
    }
}