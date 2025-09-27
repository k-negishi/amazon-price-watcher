package com.acme.amazonpricewatcher.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "amazon")
@Validated
data class AmazonItemProperties(
    val items: List<ItemConfig> = emptyList(),
    val scrape: ScrapeConfig = ScrapeConfig(),
    val dynamodb: DynamoConfig = DynamoConfig()
) {
    data class ItemConfig(
        @field:NotBlank
        val url: String,
        val label: String? = null,
        val itemNameSelector: String = "#productTitle",
        val priceSelectors: List<String> = listOf(
            "#priceblock_ourprice",
            "#priceblock_dealprice",
            "#corePriceDisplay_desktop_feature_div span.a-offscreen",
            "#centerCol span.a-offscreen"
        )
    )

    data class ScrapeConfig(
        val userAgent: String = USER_AGENT,
        val timeoutMillis: Long = 6_000,
        val retry: RetryConfig = RetryConfig()
    )

    data class RetryConfig(
        val maxAttempts: Int = 3,
        val delayMillis: Long = 500
    )

    data class DynamoConfig(
        val endpoint: String? = null,
        val region: String = "ap-northeast-1"
    )

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
    }
}
@ConfigurationProperties(prefix = "line")
@Validated
data class LineProperties(
    @field:NotBlank
    val channelToken: String = "",
    @field:NotBlank
    val targetUserId: String = "",
    val enabled: Boolean = true
)
