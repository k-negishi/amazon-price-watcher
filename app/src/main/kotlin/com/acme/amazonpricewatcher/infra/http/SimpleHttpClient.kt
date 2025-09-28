package com.acme.amazonpricewatcher.infra.http

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SimpleHttpClient @Autowired constructor(
    private val okHttpClient: OkHttpClient,
    @Value("\${amazon.item.scrape.user-agent}") private val userAgent: String,
    @Value("\${amazon.item.scrape.timeout-millis}") private val timeoutMillis: Long,
    @Value("\${amazon.item.scrape.retry.max-attempts}") private val retryMaxAttempts: Int,
    @Value("\${amazon.item.scrape.retry.delay-millis}") private val retryDelayMillis: Long
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val timeout: Duration = Duration.ofMillis(timeoutMillis)
    private val retryDelay: Duration = Duration.ofMillis(retryDelayMillis)

    suspend fun get(url: String): String = executeWithRetry {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .build()

        withContext(Dispatchers.IO) {
            okHttpClient.newBuilder()
                .callTimeout(timeout)
                .build()
                .newCall(request)
                .execute()
        }.use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${'$'}{response.code} ${'$'}{response.message}")
            }
            response.body?.string() ?: throw IOException("Empty body")
        }
    }

    private suspend fun <T> executeWithRetry(block: suspend (attempt: Int) -> T): T {
        var lastError: Throwable? = null
        repeat(retryMaxAttempts) { attemptIndex ->
            val attempt = attemptIndex + 1
            try {
                return block(attempt)
            } catch (ex: Throwable) {
                lastError = ex
                logger.warn("HTTP リクエスト失敗 attempt={}", attempt, ex)
                if (attempt >= retryMaxAttempts) {
                    throw ex
                }
                delay(retryDelay.toMillis())
            }
        }
        throw lastError ?: IllegalStateException("Unknown HTTP failure")
    }
}
