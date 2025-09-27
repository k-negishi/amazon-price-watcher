package com.acme.amazonpricewatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AmazonPriceWatcherApplication

fun main(args: Array<String>) {
    runApplication<AmazonPriceWatcherApplication>(*args)
}
