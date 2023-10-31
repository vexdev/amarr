plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "amarr"

include("app")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            version("ktor", "2.3.4")
            version("kotlin", "1.9.10")
            version("bt", "1.10")
            version("kotest", "5.7.2")

            // Libraries
            library("ktor-server-core", "io.ktor", "ktor-server-core").versionRef("ktor")
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef("ktor")
            library("ktor-server-content-negotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor-serialization-kotlinx-xml", "io.ktor", "ktor-serialization-kotlinx-xml").versionRef("ktor")
            library("ktor-server-call-logging", "io.ktor", "ktor-server-call-logging").versionRef("ktor")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("jamule", "com.vexdev", "jamule").version("0.5.0")
            library("guava", "com.google.guava", "guava").version("32.1.3-jre")
            library("kotest-runner-junit5", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-property", "io.kotest", "kotest-property").versionRef("kotest")
            library("mockk", "io.mockk", "mockk").version("1.13.8")

            // Plugins
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("jib", "com.google.cloud.tools.jib").version("3.4.0")

            // Bundles
            bundle("kotest", listOf("kotest-runner-junit5", "kotest-assertions-core", "kotest-property"))
            bundle(
                "ktor-server",
                listOf(
                    "ktor-server-core",
                    "ktor-server-netty",
                    "ktor-server-content-negotiation",
                    "ktor-serialization-kotlinx-xml",
                    "ktor-server-call-logging",
                    "ktor-serialization-kotlinx-json"
                )
            )
        }
    }
}