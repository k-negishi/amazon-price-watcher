package com.acme.amazonpricewatcher.usecase

import com.acme.amazonpricewatcher.domain.api.LineNotifier
import com.acme.amazonpricewatcher.domain.entity.PriceHistory
import com.acme.amazonpricewatcher.domain.vo.MoneyJPY
import com.acme.amazonpricewatcher.domain.vo.URL
import io.kotest.core.spec.style.StringSpec
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate

class PriceCompareUsecaseSpec : StringSpec({

    "値下がりした商品があればLINE通知を送信する" {
        val lineNotifier = mockk<LineNotifier>(relaxed = true)
        val usecase = PriceCompareUsecase(lineNotifier)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val todaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("2800")!!,
                date = today
            ),
            PriceHistory(
                url = URL.from("https://example.com/item2"),
                itemName = "Item 2",
                price = MoneyJPY.from("5000")!!,
                date = today
            )
        )

        val yesterdaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("3000")!!,
                date = yesterday
            ),
            PriceHistory(
                url = URL.from("https://example.com/item2"),
                itemName = "Item 2",
                price = MoneyJPY.from("5000")!!,
                date = yesterday
            )
        )

        usecase.execute(todaySnapshots, yesterdaySnapshots)

        val messageSlot = slot<String>()
        coVerify(exactly = 1) { lineNotifier.notify(capture(messageSlot)) }

        val message = messageSlot.captured
        assert(message.contains("Item 1"))
        assert(message.contains("3000"))
        assert(message.contains("2800"))
    }

    "値下がりがなければLINE通知を送信しない" {
        val lineNotifier = mockk<LineNotifier>(relaxed = true)
        val usecase = PriceCompareUsecase(lineNotifier)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val todaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("3000")!!,
                date = today
            )
        )

        val yesterdaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("3000")!!,
                date = yesterday
            )
        )

        usecase.execute(todaySnapshots, yesterdaySnapshots)

        coVerify(exactly = 0) { lineNotifier.notify(any()) }
    }

    "昨日のデータがない商品はスキップする" {
        val lineNotifier = mockk<LineNotifier>(relaxed = true)
        val usecase = PriceCompareUsecase(lineNotifier)

        val today = LocalDate.now()

        val todaySnapshots = listOf(
            PriceHistory(
                url = URL.from("https://example.com/item1"),
                itemName = "Item 1",
                price = MoneyJPY.from("2800")!!,
                date = today
            )
        )

        val yesterdaySnapshots = emptyList<PriceHistory>()

        usecase.execute(todaySnapshots, yesterdaySnapshots)

        coVerify(exactly = 0) { lineNotifier.notify(any()) }
    }
})
