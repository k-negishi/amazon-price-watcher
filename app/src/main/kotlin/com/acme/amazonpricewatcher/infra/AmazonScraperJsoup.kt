package com.acme.amazonpricewatcher.infra

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.api.AmazonScraper
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import com.acme.amazonpricewatcher.infra.http.SimpleHttpClient
import java.time.LocalDate
import org.jsoup.Jsoup

class AmazonScraperJsoup(
    private val httpClient: SimpleHttpClient
) : AmazonScraper {

    override suspend fun scrape(
        item: AmazonItemProperties.ItemConfig,
        targetDate: LocalDate
    ): PriceHistory {
        // httpClient で HTML を取得し、Jsoup でパースして必要な情報を抽出する
        val html = httpClient.get(item.url)
        val document = Jsoup.parse(html)

        val title = document.select(item.itemNameSelector)
            .firstOrNull()
            ?.text()
            ?.trim()
            ?.ifBlank { null }
            ?: throw IllegalStateException("商品名を取得できませんでした: ${'$'}{item.url}")

        val priceText = item.priceSelectors.asSequence()
            .mapNotNull { selector ->
                document
                    .select(selector)
                    .firstOrNull()
                    ?.text()
                    ?.trim()
            }
            .firstOrNull { it.isNotBlank() }
            ?: throw IllegalStateException("価格を取得できませんでした: ${'$'}{item.url}")

        val price = MoneyJPY.from(priceText)
            ?: throw IllegalStateException("価格のパースに失敗しました: ${'$'}priceText")

        return PriceHistory(
            url = URL.from(item.url),
            itemName = title,
            price = price,
            date = targetDate,
        )
    }
}
