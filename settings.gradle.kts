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

            // Libraries
            library("ktor-server-core", "io.ktor", "ktor-server-core").versionRef("ktor")
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef("ktor")
            library("ktor-server-content-negotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor-serialization-kotlinx-xml", "io.ktor", "ktor-serialization-kotlinx-xml").versionRef("ktor")
            library("ktor-server-call-logging", "io.ktor", "ktor-server-call-logging").versionRef("ktor")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("amule-ec", "com.iukonline.amule", "javaamuleec").version("0.5.1-SNAPSHOT")
            library("bt-bencoding", "com.github.atomashpolskiy", "bt-bencoding").version("1.10")

            // Plugins
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            // Bundles
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