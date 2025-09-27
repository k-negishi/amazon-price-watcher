package com.acme.amazonpricewatcher.infra.http

import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.Duration

class SimpleHttpClient(
    private val okHttpClient: OkHttpClient,
    private val userAgent: String,
    private val timeout: Duration,
    private val retryMaxAttempts: Int,
    private val retryDelay: Duration
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
