package com.acme.pricechecker.domain.vo

import java.time.LocalDateTime

@JvmInline
value class TTL private constructor(
    val v: Long
) {
    companion object {
        fun default(): TTL = TTL(LocalDateTime.now().plusDays(7).toEpochSecond(java.time.ZoneOffset.ofHours(9)))
    }
}
