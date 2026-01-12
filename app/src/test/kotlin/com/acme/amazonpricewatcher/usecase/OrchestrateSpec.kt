package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import io.kotest.core.spec.style.StringSpec
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.time.LocalDate

class OrchestrateSpec : StringSpec({

    "全体のワークフローが正常に動作する" {
        val fetchAmazonPriceUsecase = mockk<FetchAmazonPriceUsecase>()
        val fetchPriceHistoryUsecase = mockk<FetchPriceHistoryUsecase>()
        val priceCompareUsecase = mockk<PriceCompareUsecase>()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val todaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("2800")!!,
                date = today
            )
        )

        val yesterdaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("3000")!!,
                date = yesterday
            )
        )

        coEvery { fetchAmazonPriceUsecase.execute(any()) } returns todaySnapshots
        coEvery { fetchPriceHistoryUsecase.execute(any()) } returns yesterdaySnapshots
        coJustRun { priceCompareUsecase.execute(any(), any()) }

        val orchestrate = Orchestrate(
            fetchAmazonPriceUsecase = fetchAmazonPriceUsecase,
            fetchPriceHistoryUsecase = fetchPriceHistoryUsecase,
            priceCompareUsecase = priceCompareUsecase
        )

        orchestrate.execute()

        coVerifyOrder {
            fetchAmazonPriceUsecase.execute(any())
            fetchPriceHistoryUsecase.execute(any())
            priceCompareUsecase.execute(todaySnapshots, yesterdaySnapshots)
        }
    }
})
