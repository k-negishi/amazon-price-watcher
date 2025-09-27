package com.acme.amazonpricewatcher.domain.repository

import com.acme.amazonpricewatcher.domain.vo.URL
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import java.time.LocalDate

interface PriceHistoryRepository {
    suspend fun save(snapshot: PriceHistory)
    suspend fun findById(url: URL, date: LocalDate): PriceHistory?
}
