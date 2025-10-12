package com.acme.pricechecker.lambda.fetchprice.infra

import com.acme.pricechecker.domain.api.AmazonScraper
import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.domain.vo.MoneyJPY
import com.acme.pricechecker.lambda.fetchprice.usecase.ItemConfig
import java.time.LocalDate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class AmazonScraperJsoup : AmazonScraper {

    private val httpClient = SimpleHttpClient()

    override suspend fun scrape(item: ItemConfig, targetDate: LocalDate): PriceHistory {
        val html = httpClient.get(item.url.value)
        val doc = Jsoup.parse(html)

        // Find price from a specific element (this is just an example, the selector will vary)
        val priceText = doc.select("#priceblock_ourprice").text()
        val price = MoneyJPY.from(priceText) ?: MoneyJPY.from("0")!!

        return PriceHistory(
            url = item.url,
            itemName = item.name,
            price = price,
            date = targetDate
        )
    }
}

private class SimpleHttpClient {
    private val client = OkHttpClient()

    fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to fetch URL: $url")
            }
            return response.body!!.string()
        }
    }
}
