package com.acme.amazonpricewatcher.config

import com.acme.amazonpricewatcher.domain.`interface`.AmazonScraper
import com.acme.amazonpricewatcher.domain.`interface`.LineNotifier
import com.acme.amazonpricewatcher.infra.AmazonScraperJsoup
import com.acme.amazonpricewatcher.infra.LineNotifierLineSdk
import com.acme.amazonpricewatcher.infra.PriceHistoryDynamoRepository
import com.acme.amazonpricewatcher.infra.http.SimpleHttpClient
import com.acme.amazonpricewatcher.usecase.FetchAmazonPriceUsecase
import com.acme.amazonpricewatcher.usecase.Orchestrate
import com.acme.amazonpricewatcher.usecase.PriceCompareUsecase
import java.net.URI
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
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
    fun priceHistoryRepository(
        client: DynamoDbAsyncClient,
        @Value($$"${PRICE_TABLE:PriceHistory}") tableName: String
    ): PriceHistoryDynamoRepository {
        return PriceHistoryDynamoRepository(
            client = client,
            tableName = tableName
        )
    }

    @Bean
    fun simpleHttpClient(properties: AmazonItemProperties, okHttpClient: OkHttpClient): SimpleHttpClient =
        SimpleHttpClient(
            okHttpClient = okHttpClient,
            userAgent = properties.scrape.userAgent,
            timeout = Duration.ofMillis(properties.scrape.timeoutMillis),
            retryMaxAttempts = properties.scrape.retry.maxAttempts,
            retryDelay = Duration.ofMillis(properties.scrape.retry.delayMillis)
        )

    @Bean
    fun amazonScraper(simpleHttpClient: SimpleHttpClient): AmazonScraper = AmazonScraperJsoup(simpleHttpClient)

    @Bean
    fun collectToday(
        properties: AmazonItemProperties,
        scraper: AmazonScraper,
        priceHistoryRepository: PriceHistoryDynamoRepository,
        clock: Clock,
        zoneId: ZoneId,
        @Value($$"${COLLECT_PARALLEL:4}") parallelism: Int
    ) = FetchAmazonPriceUsecase(
        properties = properties,
        scraper = scraper,
        repository = priceHistoryRepository,
        clock = clock,
        zoneId = zoneId,
        parallelism = parallelism,
        dispatcher = Dispatchers.IO
    )

    @Bean
    fun compareAndNotify(
        properties: AmazonItemProperties,
        repository: PriceHistoryDynamoRepository,
        clock: Clock,
        zoneId: ZoneId
    ) = PriceCompareUsecase(
        properties = properties,
        repository = repository,
        clock = clock,
        zoneId = zoneId
    )

    @Bean
    fun orchestrate(
        fetchAmazonPriceUsecase: FetchAmazonPriceUsecase,
        priceCompareUsecase: PriceCompareUsecase,
        lineNotifier: LineNotifier,
    ) = Orchestrate(
        fetchAmazonPriceUsecase = fetchAmazonPriceUsecase,
        priceCompareUsecase = priceCompareUsecase,
        lineNotifier = lineNotifier,
    )


    @Bean
    @Profile("!local")
    fun lineNotifierLineSdk(lineProperties: LineProperties): LineNotifier {
        return LineNotifierLineSdk(lineProperties)
    }
}
