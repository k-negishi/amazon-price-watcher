package com.acme.pricechecker.domain.repository

import com.acme.pricechecker.domain.vo.URL
import com.acme.pricechecker.domain.entity.PriceHistory
import java.time.LocalDate

interface PriceHistoryRepository {
    suspend fun save(snapshot: PriceHistory)
    suspend fun findById(url: URL, date: LocalDate): PriceHistory?
}
