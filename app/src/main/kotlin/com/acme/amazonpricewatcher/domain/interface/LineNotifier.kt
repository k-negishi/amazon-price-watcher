package com.acme.amazonpricewatcher.domain.`interface`

interface LineNotifier {
    suspend fun notify(message: String)
}