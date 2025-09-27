package com.acme.amazonpricewatcher.domain.entity

import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
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