package com.acme.amazonpricewatcher.infra

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.api.AmazonScraper
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import com.acme.amazonpricewatcher.fw.logger
import com.acme.amazonpricewatcher.infra.http.SimpleHttpClient
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class AmazonScraperJsoup(
    private val httpClient: SimpleHttpClient
) : AmazonScraper {
    private val objectMapper = jacksonObjectMapper()

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
            ?: throw IllegalStateException("商品名を取得できませんでした: ${item.url}")

        val priceText = extractPriceFromSelectors(document, item.priceSelectors)
            ?: extractPriceFromJsonLd(document)
            ?: run {
                logger.warn("価格セレクタに一致しませんでした url={} selectors={}", item.url, item.priceSelectors)
                throw IllegalStateException("価格を取得できませんでした: ${item.url}")
            }

        val price = MoneyJPY.from(priceText)
            ?: throw IllegalStateException("価格のパースに失敗しました: $priceText")

        val snapshot = PriceHistory(
            url = URL.from(item.url),
            itemName = title,
            price = price,
            date = targetDate,
        )

        logger.info("Scraped item: {}", snapshot)
        return snapshot
    }

    private fun extractPriceFromSelectors(document: org.jsoup.nodes.Document, selectors: List<String>): String? {
        return selectors.asSequence()
            .mapNotNull { selector ->
                document
                    .select(selector)
                    .firstOrNull()
                    ?.text()
                    ?.trim()
            }
            .firstOrNull { it.isNotBlank() }
    }

    private fun extractPriceFromJsonLd(document: org.jsoup.nodes.Document): String? {
        val scripts = document.select("script[type=application/ld+json]")
        for (script in scripts) {
            val json = script.data().trim()
            if (json.isBlank()) {
                continue
            }
            try {
                val node = objectMapper.readTree(json)
                val price = extractPriceFromJsonLdNode(node)
                if (!price.isNullOrBlank()) {
                    logger.info("JSON-LD から価格を取得しました url={}", document.location())
                    return price
                }
            } catch (ex: Exception) {
                logger.debug("JSON-LD の解析に失敗しました", ex)
            }
        }
        return null
    }

    private fun extractPriceFromJsonLdNode(node: JsonNode?): String? {
        if (node == null || node.isNull) {
            return null
        }
        if (node.isArray) {
            node.forEach { child ->
                val price = extractPriceFromJsonLdNode(child)
                if (!price.isNullOrBlank()) {
                    return price
                }
            }
            return null
        }
        if (!node.isObject) {
            return null
        }

        val offersPrice = extractPriceFromOfferNode(node.get("offers"))
        if (!offersPrice.isNullOrBlank()) {
            return offersPrice
        }

        val priceSpec = extractPriceFromOfferNode(node.get("priceSpecification"))
        if (!priceSpec.isNullOrBlank()) {
            return priceSpec
        }

        val directPrice = node.get("price")
        if (directPrice != null && directPrice.isValueNode) {
            return directPrice.asText()
        }

        val graphPrice = extractPriceFromJsonLdNode(node.get("@graph"))
        if (!graphPrice.isNullOrBlank()) {
            return graphPrice
        }

        val fields = node.fields()
        while (fields.hasNext()) {
            val (_, value) = fields.next()
            val price = extractPriceFromJsonLdNode(value)
            if (!price.isNullOrBlank()) {
                return price
            }
        }
        return null
    }

    private fun extractPriceFromOfferNode(node: JsonNode?): String? {
        if (node == null || node.isNull) {
            return null
        }
        if (node.isArray) {
            node.forEach { child ->
                val price = extractPriceFromOfferNode(child)
                if (!price.isNullOrBlank()) {
                    return price
                }
            }
            return null
        }
        if (node.isObject) {
            val priceNode = node.get("price")
            if (priceNode != null && priceNode.isValueNode) {
                return priceNode.asText()
            }
            val priceSpecNode = node.get("priceSpecification")
            if (priceSpecNode != null) {
                val price = extractPriceFromOfferNode(priceSpecNode)
                if (!price.isNullOrBlank()) {
                    return price
                }
            }
        }
        return null
    }
}
