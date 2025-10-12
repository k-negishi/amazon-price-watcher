package com.acme.pricechecker.lambda.fetchprice

import com.acme.pricechecker.domain.api.AmazonScraper
import com.acme.pricechecker.domain.repository.PriceHistoryRepository
import com.acme.pricechecker.lambda.fetchprice.infra.AmazonScraperJsoup
import com.acme.pricechecker.lambda.fetchprice.infra.PriceHistoryDynamoRepository
import com.acme.pricechecker.lambda.fetchprice.usecase.FetchPriceUsecase
import org.koin.dsl.module

val fetchPriceModule = module {
    single { FetchPriceUsecase(get(), get()) }
    single<AmazonScraper> { AmazonScraperJsoup() }
    single<PriceHistoryRepository> { PriceHistoryDynamoRepository() }
}
