plugins {
    kotlin("jvm") version "2.4.20"
    application
}

group = "org.rameshwx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("sequential_requests.SequentialRequestsKt")
}

tasks.test {
    useJUnitPlatform()
}
