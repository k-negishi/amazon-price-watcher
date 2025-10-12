package com.acme.pricechecker.lambda.getpricehistory

import com.acme.pricechecker.lambda.getpricehistory.usecase.GetPriceHistoryUsecase
import org.koin.dsl.module

val getPriceHistoryModule = module {
    single { GetPriceHistoryUsecase(get()) }
}
