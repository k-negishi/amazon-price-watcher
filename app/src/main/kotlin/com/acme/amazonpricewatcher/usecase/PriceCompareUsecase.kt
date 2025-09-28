package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.api.LineNotifier
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PriceCompareUsecase @Autowired constructor(
    private val lineNotifier: LineNotifier
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    data class PriceDrop(
        val url: String,
        val itemName: String,
        val yesterdayPrice: Long,
        val todayPrice: Long
    )

    /**
     * 今日と昨日の価格を比較し、値下がりがあればLINE通知を送信する
     */
    suspend fun execute(
        todaySnapshots: List<PriceHistory>,
        yesterdaySnapshots: List<PriceHistory>
    ) {
        val priceDrops = comparePrices(todaySnapshots, yesterdaySnapshots)

        // 値下がりがあればLINE通知
        if (priceDrops.isNotEmpty()) {
            val message = buildMessage(priceDrops)
            lineNotifier.notify(message)
        }
    }

    /**
     * 値下がり商品のリストから通知メッセージを構築する
     */
    private fun buildMessage(
        priceDrops: List<PriceDrop>
    ): String {
        val header = "[AmazonPriceWatcher: 値下がり通知]"
        val lines = priceDrops.joinToString(separator = "\n\n") { drop ->
            "・${drop.itemName}\n  昨日: ${drop.yesterdayPrice} → 今日: ${drop.todayPrice}\n  ${drop.url}"
        }
        return listOf(header, lines).joinToString(separator = "\n\n")
    }

    /**
     * 今日と昨日の価格を比較し、値下がりした商品のリストを返却する
     */
    private fun comparePrices(
        todaySnapshots: List<PriceHistory>,
        yesterdaySnapshots: List<PriceHistory>
    ): List<PriceDrop> {
        val yesterdayPriceMap = yesterdaySnapshots.associateBy { it.url.value }
        val priceDrops = mutableListOf<PriceDrop>()

        for (todaySnapshot in todaySnapshots) {
            val yesterdaySnapshot = yesterdayPriceMap[todaySnapshot.url.value]

            if (yesterdaySnapshot == null) {
                logger.debug("昨日の価格が見つかりません url={}", todaySnapshot.url.value)
                continue
            }

            logger.debug(
                "価格比較 url={} 今日={} 昨日={}",
                todaySnapshot.url.value, todaySnapshot.price.v, yesterdaySnapshot.price.v
            )

            // 今日の価格が昨日より安い場合
            if (todaySnapshot.price.v < yesterdaySnapshot.price.v) {
                priceDrops.add(
                    PriceDrop(
                        url = todaySnapshot.url.value,
                        itemName = todaySnapshot.itemName,
                        yesterdayPrice = yesterdaySnapshot.price.v,
                        todayPrice = todaySnapshot.price.v
                    )
                )
                logger.info(
                    "値下がりを検出 url={} {}円 → {}円",
                    todaySnapshot.url.value, yesterdaySnapshot.price.v, todaySnapshot.price.v
                )
            }
        }

        return priceDrops
    }
}
