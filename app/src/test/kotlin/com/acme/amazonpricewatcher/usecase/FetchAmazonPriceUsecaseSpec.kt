package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.api.AmazonScraper
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.repository.PriceHistoryRepository
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate

class FetchAmazonPriceUsecaseSpec : StringSpec({

    "複数商品の価格を並列で取得してリポジトリに保存する" {
        val scraper = mockk<AmazonScraper>()
        val repository = mockk<PriceHistoryRepository>(relaxed = true)

        val properties = AmazonItemProperties(
            items = listOf(
                AmazonItemProperties.ItemConfig(url = "https://example.com/item1"),
                AmazonItemProperties.ItemConfig(url = "https://example.com/item2")
            )
        )

        val today = LocalDate.now()

        val item1 = PriceHistory(
            url = URL.from("https://example.com/item1"),
            itemName = "Item 1",
            price = MoneyJPY.from("2800")!!,
            date = today
        )

        val item2 = PriceHistory(
            url = URL.from("https://example.com/item2"),
            itemName = "Item 2",
            price = MoneyJPY.from("5000")!!,
            date = today
        )

        coEvery { scraper.scrape(properties.items[0], today) } returns item1
        coEvery { scraper.scrape(properties.items[1], today) } returns item2

        val usecase = FetchAmazonPriceUsecase(properties, scraper, repository)

        val result = usecase.execute(today)

        result shouldHaveSize 2
        result[0] shouldBe item1
        result[1] shouldBe item2

        coVerify(exactly = 1) { repository.save(item1) }
        coVerify(exactly = 1) { repository.save(item2) }
    }

    "スクレイピングに失敗した商品は結果から除外される" {
        val scraper = mockk<AmazonScraper>()
        val repository = mockk<PriceHistoryRepository>(relaxed = true)

        val properties = AmazonItemProperties(
            items = listOf(
                AmazonItemProperties.ItemConfig(url = "https://example.com/item1"),
                AmazonItemProperties.ItemConfig(url = "https://example.com/item2")
            )
        )

        val today = LocalDate.now()

        val item1 = PriceHistory(
            url = URL.from("https://example.com/item1"),
            itemName = "Item 1",
            price = MoneyJPY.from("2800")!!,
            date = today
        )

        coEvery { scraper.scrape(properties.items[0], today) } returns item1
        coEvery { scraper.scrape(properties.items[1], today) } throws RuntimeException("Scraping failed")

        val usecase = FetchAmazonPriceUsecase(properties, scraper, repository)

        val result = usecase.execute(today)

        result shouldHaveSize 1
        result[0] shouldBe item1

        coVerify(exactly = 1) { repository.save(item1) }
    }
})
