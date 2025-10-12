package com.acme.pricechecker.domain.entity

import com.acme.pricechecker.domain.vo.MoneyJPY
import com.acme.pricechecker.domain.vo.URL
import java.time.LocalDate

data class PriceHistory(
    /**
     * 商品のURL
     */
    val url: URL,

    /**
     * 商品名
     */
    val itemName: String,

    /**
     * 価格
     */
    val price: MoneyJPY,

    /**
     * 基準日
     */
    val date: LocalDate,
)
