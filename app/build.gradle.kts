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
    implementation(libs.jamule)
    implementation(libs.guava)
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.5")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.3.5")
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.mockk)
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
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