package com.acme.pricechecker.lambda.fetchprice.infra

import com.acme.pricechecker.domain.entity.PriceHistory
import com.acme.pricechecker.domain.repository.PriceHistoryRepository
import com.acme.pricechecker.domain.vo.MoneyJPY
import com.acme.pricechecker.domain.vo.URL
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.net.URI
import java.time.LocalDate

class PriceHistoryDynamoRepository : PriceHistoryRepository {

    private val dynamoDbClient: DynamoDbClient by lazy {
        val region = System.getenv("AWS_REGION") ?: "ap-northeast-1"
        val builder = DynamoDbClient.builder()
            .region(Region.of(region))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())

        System.getenv("DYNAMODB_ENDPOINT")?.let {
            builder.endpointOverride(URI.create(it))
        }

        builder.build()
    }

    private val tableName = System.getenv("PRICE_HISTORY_TABLE_NAME") ?: "PriceHistory"

    override suspend fun save(snapshot: PriceHistory) {
        val item = mapOf(
            "url" to AttributeValue.builder().s(snapshot.url.value).build(),
            "date" to AttributeValue.builder().s(snapshot.date.toString()).build(),
            "itemName" to AttributeValue.builder().s(snapshot.itemName).build(),
            "price" to AttributeValue.builder().n(snapshot.price.v.toString()).build()
        )
        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build()
        dynamoDbClient.putItem(request)
    }

    override suspend fun findById(url: URL, date: LocalDate): PriceHistory? {
        val key = mapOf(
            "url" to AttributeValue.builder().s(url.value).build(),
            "date" to AttributeValue.builder().s(date.toString()).build()
        )
        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        val response = dynamoDbClient.getItem(request)
        if (!response.hasItem()) {
            return null
        }

        val item = response.item()
        return PriceHistory(
            url = URL.from(item["url"]!!.s()),
            itemName = item["itemName"]!!.s(),
            price = MoneyJPY.from(item["price"]!!.n())!!,
            date = LocalDate.parse(item["date"]!!.s())
        )
    }
}
