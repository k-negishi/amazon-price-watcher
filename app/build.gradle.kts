import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.spring.boot.bom))
    implementation(platform(libs.spring.cloud.bom))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.json)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.cloud.starter.function.webflux)
    implementation(libs.spring.cloud.function.kotlin)
    implementation(libs.aws.lambda.core)

    implementation(libs.aws.sdk.dynamodb)
    implementation(libs.aws.sdk.regions)
    implementation(libs.aws.sdk.auth)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.jdk8)

    implementation(libs.okhttp)
    implementation(libs.jsoup)
    implementation(libs.line.messaging.api.client)
    implementation(libs.jackson.module.kotlin)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.localstack)
    testImplementation(libs.testcontainers.wiremock)
    testImplementation(libs.aws.sdk.dynamodb)
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("kotest.assertions.collection.print.size", 20)
}

tasks.named<Test>("test") {
    enabled = false
}

tasks.register<Test>("unitTest") {
    description = "Runs unit layer Kotest specs"
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    useJUnitPlatform()
    filter {
        includeTestsMatching("com.acme.amazonpricewatcher.usecase.*")
    }
}

tasks.register<Test>("e2eTest") {
    description = "Runs E2E specs with Testcontainers"
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    useJUnitPlatform()
    filter {
        includeTestsMatching("com.acme.amazonpricewatcher.e2e.*")
    }
    shouldRunAfter("unitTest")
}

tasks.named("check") {
    dependsOn("unitTest", "e2eTest")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

detekt {
    baseline = file("detekt-baseline.xml")
}

//koverReport {
//    defaults {
//        xml {
//            onCheck = false
//            setReportFile(layout.buildDirectory.file("reports/kover/coverage.xml"))
//        }
//        html {
//            onCheck = false
//            setReportDir(layout.buildDirectory.dir("reports/kover/html"))
//        }
//        verify {
//            rule {
//                bound {
//                    minValue = 60
//                }
//            }
//        }
//    }
//}
