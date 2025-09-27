package com.acme.amazonpricewatcher.domain.api

interface LineNotifier {
    suspend fun notify(message: String)
}