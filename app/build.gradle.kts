plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.amule.ec)
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
