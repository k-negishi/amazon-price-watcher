package com.acme.amazonpricewatcher.domain.vo

import java.time.LocalDateTime

@JvmInline
value class TTL private constructor(
    val v: Long
) {
    companion object {
        fun default(): TTL = TTL(LocalDateTime.now().plusDays(7).toEpochSecond(java.time.ZoneOffset.ofHours(9)))
    }
}
