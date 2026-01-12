package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.repository.PriceHistoryRepository
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate

class FetchPriceHistoryUsecaseSpec : StringSpec({

    "複数商品の価格履歴を取得する" {
        val repository = mockk<PriceHistoryRepository>()

        val properties = AmazonItemProperties(
            items = listOf(
                AmazonItemProperties.ItemConfig(url = "https://example.com/item1"),
                AmazonItemProperties.ItemConfig(url = "https://example.com/item2")
            )
        )

        val yesterday = LocalDate.now().minusDays(1)

        val item1 = PriceHistory(
            url = URL.from("https://example.com/item1"),
            itemName = "Item 1",
            price = MoneyJPY.from("3000")!!,
            date = yesterday
        )

        val item2 = PriceHistory(
            url = URL.from("https://example.com/item2"),
            itemName = "Item 2",
            price = MoneyJPY.from("5000")!!,
            date = yesterday
        )

        coEvery { repository.findById(URL.from("https://example.com/item1"), yesterday) } returns item1
        coEvery { repository.findById(URL.from("https://example.com/item2"), yesterday) } returns item2

        val usecase = FetchPriceHistoryUsecase(properties, repository)

        val result = usecase.execute(yesterday)

        result shouldHaveSize 2
        result[0] shouldBe item1
        result[1] shouldBe item2
    }

    "取得できなかった商品は結果から除外される" {
        val repository = mockk<PriceHistoryRepository>()

        val properties = AmazonItemProperties(
            items = listOf(
                AmazonItemProperties.ItemConfig(url = "https://example.com/item1"),
                AmazonItemProperties.ItemConfig(url = "https://example.com/item2")
            )
        )

        val yesterday = LocalDate.now().minusDays(1)

        val item1 = PriceHistory(
            url = URL.from("https://example.com/item1"),
            itemName = "Item 1",
            price = MoneyJPY.from("3000")!!,
            date = yesterday
        )

        coEvery { repository.findById(URL.from("https://example.com/item1"), yesterday) } returns item1
        coEvery { repository.findById(URL.from("https://example.com/item2"), yesterday) } returns null

        val usecase = FetchPriceHistoryUsecase(properties, repository)

        val result = usecase.execute(yesterday)

        result shouldHaveSize 1
        result[0] shouldBe item1
    }
})
