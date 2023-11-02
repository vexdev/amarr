plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jib)
    application
}

println("Version is $version")

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.jamule)
    implementation(libs.guava)
    implementation(libs.logback)
    implementation(libs.commons.text)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation(libs.ktor.server.test.host.jvm)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlin.test.junit)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("amarr.AppKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

jib {
    from {
        image = "openjdk:17-jdk-slim"
    }
    to {
        image = "vexdev/amarr"
        tags = setOf(version.toString())
        auth {
            username = System.getenv("DOCKER_USERNAME")
            password = System.getenv("DOCKER_PASSWORD")
        }
    }
}