package com.acme.pricechecker.domain.api

interface LineNotifier {
    suspend fun notify(message: String)
}
