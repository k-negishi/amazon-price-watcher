package com.acme.amazonpricewatcher.infra.http

import com.acme.amazonpricewatcher.config.AmazonItemProperties
import com.acme.amazonpricewatcher.fw.logger
import java.io.IOException
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component

@Component
class SimpleHttpClient(
    private val okHttpClient: OkHttpClient,
    private val properties: AmazonItemProperties
) {
    private val userAgent = properties.scrape.userAgent
    private val timeout = Duration.ofMillis(properties.scrape.timeoutMillis)
    private val retryMaxAttempts = properties.scrape.retry.maxAttempts
    private val retryDelay = Duration.ofMillis(properties.scrape.retry.delayMillis)

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
