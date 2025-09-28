package com.acme.amazonpricewatcher.usecase

import java.time.LocalDate
import java.time.ZoneId


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * 今日の価格を収集し、昨日と比較して値下がりがあれば通知する一連の処理を実行します。
 */
@Service
class Orchestrate @Autowired constructor(
    private val fetchAmazonPriceUsecase: FetchAmazonPriceUsecase,
    private val fetchPriceHistoryUsecase: FetchPriceHistoryUsecase,
    private val priceCompareUsecase: PriceCompareUsecase,
) {
    suspend fun execute() {
        val today = LocalDate.now(ZoneId.of("Asia/Tokyo"))
        val yesterday = today.minusDays(1)

        val todayResult = fetchAmazonPriceUsecase.execute(targetDate = today)
        val yesterdayResult = fetchPriceHistoryUsecase.execute(targetDate = yesterday)

        priceCompareUsecase.execute(
            todaySnapshots = todayResult,
            yesterdaySnapshots = yesterdayResult
        )
    }
}
