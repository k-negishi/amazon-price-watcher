package com.acme.pricechecker.domain.vo

@JvmInline
value class URL(val value: String) {
    init {
        require(value.isNotBlank()) { "ItemId must not be blank" }
    }
    override fun toString(): String = value
    companion object {
        fun from(url: String): URL = URL(url)
    }
}
