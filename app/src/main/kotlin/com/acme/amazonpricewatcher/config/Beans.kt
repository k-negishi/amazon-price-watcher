package com.acme.amazonpricewatcher.config

import com.acme.amazonpricewatcher.domain.api.AmazonScraper
import com.acme.amazonpricewatcher.domain.api.LineNotifier
import com.acme.amazonpricewatcher.infra.AmazonScraperJsoup
import com.acme.amazonpricewatcher.infra.LineNotifierLineSdk
import com.acme.amazonpricewatcher.infra.http.SimpleHttpClient
import java.net.URI
import java.time.Clock
import java.time.ZoneId
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

@Configuration
@EnableConfigurationProperties(value = [AmazonItemProperties::class, LineProperties::class])
class Beans {

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Bean
    fun zoneId(): ZoneId = ZoneId.of("Asia/Tokyo")

    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("Asia/Tokyo"))

    @Bean
    fun dynamoDbAsyncClient(
        properties: AmazonItemProperties
    ): DynamoDbAsyncClient {
        val builder = DynamoDbAsyncClient.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.of(properties.dynamodb.region))

        properties.dynamodb.endpoint?.takeIf { it.isNotBlank() }?.let {
            builder.endpointOverride(URI.create(it))
        }

        return builder.build()
    }

    @Bean
    fun amazonScraper(
        simpleHttpClient: SimpleHttpClient
    ): AmazonScraper = AmazonScraperJsoup(simpleHttpClient)

    @Bean
    fun lineNotifierLineSdk(
        lineProperties: LineProperties
    ): LineNotifier {
        return LineNotifierLineSdk(lineProperties)
    }
}