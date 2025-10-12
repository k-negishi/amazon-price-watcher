package com.acme.pricechecker.domain.vo

data class MoneyJPY private constructor(
    val v: Long
) {
    init {
        require(v >= 0) { "金額は0以上である必要があります" }
    }

    companion object {
        private val DIGIT_REGEX = Regex("[0-9]+")

        fun from(raw: String): MoneyJPY? {
            val digits = DIGIT_REGEX.findAll(raw)
                .joinToString(separator = "") { it.value }
            if (digits.isBlank()) {
                return null
            }
            return MoneyJPY(digits.toLong())
        }
    }
}
