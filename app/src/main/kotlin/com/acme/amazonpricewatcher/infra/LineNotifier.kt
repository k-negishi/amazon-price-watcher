package com.acme.amazonpricewatcher.infra

import com.acme.amazonpricewatcher.config.LineProperties
import com.acme.amazonpricewatcher.domain.`interface`.LineNotifier
import com.linecorp.bot.messaging.client.MessagingApiClient
import com.linecorp.bot.messaging.model.PushMessageRequest
import com.linecorp.bot.messaging.model.TextMessage
import java.util.UUID
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils

class LineNotifierLineSdk(
    private val properties: LineProperties
) : LineNotifier {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val client: MessagingApiClient by lazy {
        MessagingApiClient.builder(properties.channelToken).build()
    }

    override suspend fun notify(
        message: String
    ) {
        if (!properties.enabled) {
            logger.info("LINE 通知は無効化されています")
            return
        }
        if (!StringUtils.hasText(properties.targetUserId)) {
            logger.warn("targetUserId が設定されていないため通知をスキップします")
            return
        }

        val text = TextMessage(message)

        // PushMessageRequest は Builder でもコンストラクタでもOK。ここでは Builder を使用。
        val request: PushMessageRequest? = PushMessageRequest.Builder(
            properties.targetUserId,
            listOf(text)
        ).build()

        // CompletableFuture を await して送信完了を待つ（例外は上に投げる/必要なら catch）
        client.pushMessage(
            UUID.randomUUID(),
            request
        ).await()

        logger.info("LINE 通知送信完了")
    }
}
