package com.acme.amazonpricewatcher.infra

import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.repository.PriceHistoryRepository
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.TTL
import com.acme.amazonpricewatcher.domain.vo.URL
import java.time.LocalDate
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest

class PriceHistoryDynamoRepository(
    private val client: DynamoDbAsyncClient,
    private val tableName: String
) : PriceHistoryRepository {

    override suspend fun save(snapshot: PriceHistory) {

        val item = mapOf(
            "id" to buildStringAttribute(compositeKey(snapshot.url, snapshot.date)),
            "url" to buildStringAttribute(snapshot.url.value),
            "date" to buildStringAttribute(snapshot.date.toString()),
            "itemName" to buildStringAttribute(snapshot.itemName),
            "price" to buildNumberAttribute(snapshot.price.v.toString()),
            "ttl" to buildNumberAttribute(TTL.default().toString()),
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build()

        // 保存処理を実行
        client.putItem(request).await()
    }

    /**
     * ID（URL + 日付）で PriceSnapshot を取得する
     */
    override suspend fun findById(
        url: URL,
        date: LocalDate
    ): PriceHistory? {
        val key = compositeKey(url, date)
        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(
                mapOf(
                    "id" to AttributeValue.builder().s(key).build()
                )
            ).build()

        val response = client.getItem(request).await()
        val item = response.item() ?: return null

        val url = item["url"]?.s() ?: return null
        val price = item["price"]?.n()?.let { MoneyJPY.from(it) } ?: return null
        val itemName = item["title"]?.s() ?: return null
        val date = item["date"]?.s()?.let(LocalDate::parse) ?: return null

        return PriceHistory(
            url = URL.from(url),
            itemName = itemName,
            price = price,
            date = date,
        )
    }

    private fun buildStringAttribute(valueProvider: String): AttributeValue =
        AttributeValue.builder().s(valueProvider).build()

    private fun buildNumberAttribute(valueProvider: String): AttributeValue =
        AttributeValue.builder().n(valueProvider).build()

    private fun compositeKey(
        url: URL,
        date: LocalDate
    ): String = "${url.value}#${date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)}"
}
