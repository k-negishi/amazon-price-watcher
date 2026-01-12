import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kover)
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

tasks.named("check") {
    dependsOn("unitTest")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

kover {
    reports {
        total {
            xml {
                onCheck.set(false)
                xmlFile.set(layout.buildDirectory.file("reports/kover/coverage.xml"))
            }
            html {
                onCheck.set(false)
                htmlDir.set(layout.buildDirectory.dir("reports/kover/html"))
            }
            verify {
                onCheck.set(true)
                rule {
                    bound {
                        minValue = 60
                    }
                }
            }
        }
    }
}
