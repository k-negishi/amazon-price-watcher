package com.acme.pricechecker.lambda.fetchprice.usecase

import com.acme.pricechecker.domain.api.AmazonScraper
import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.domain.repository.PriceHistoryRepository
import com.acme.pricechecker.domain.vo.MoneyJPY
import com.acme.pricechecker.domain.vo.URL
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FetchPriceUsecaseTest {

    private val scraper: AmazonScraper = mockk()
    private val repository: PriceHistoryRepository = mockk()
    private val usecase = FetchPriceUsecase(scraper, repository)

    @Test
    fun `execute should scrape and save price history for each item`() = runBlocking {
        // Given
        val targetDate = LocalDate.of(2023, 10, 27)
        val dummyHistory = PriceHistory(
            url = URL("https://www.amazon.co.jp/dp/B000000000"),
            itemName = "Sample Item 1",
            price = MoneyJPY.from("1000")!!,
            date = targetDate
        )

        coEvery { scraper.scrape(any(), any()) } returns dummyHistory
        coEvery { repository.save(any()) } returns Unit

        // When
        usecase.execute(targetDate)

        // Then
        coVerify(exactly = 2) { scraper.scrape(any<ItemConfig>(), targetDate) }
        coVerify(exactly = 2) { repository.save(dummyHistory) }
    }
}
