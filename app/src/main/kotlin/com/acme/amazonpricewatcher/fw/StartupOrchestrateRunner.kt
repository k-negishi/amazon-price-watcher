package com.acme.amazonpricewatcher.fw

import com.acme.amazonpricewatcher.usecase.Orchestrate
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app", name = ["run-on-startup"], havingValue = "true")
class StartupOrchestrateRunner(
    private val orchestrate: Orchestrate,
    private val applicationContext: ApplicationContext
) : ApplicationRunner {
    override fun run(args: org.springframework.boot.ApplicationArguments) {
        logger.info("Starting orchestrate on application startup")
        runBlocking {
            orchestrate.execute()
        }
        logger.info("Orchestrate completed")
        val exitCode = SpringApplication.exit(applicationContext)
        logger.info("Shutting down application (exitCode={})", exitCode)
        System.exit(exitCode)
    }
}
