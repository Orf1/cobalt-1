val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val inquirer_version: String by project
val clikt_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "dev.orf1"
version = "1.0.0"
application {
    mainClass.set("dev.orf1.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:$inquirer_version")
    implementation("com.github.ajalt.clikt:clikt:$clikt_version")
    implementation("io.ktor:ktor-server-auth-jvm:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-core-jvm:2.0.0-beta-1")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Implementation-Title", "Cobalt"))
            attributes(Pair("Implementation-Version", archiveVersion))
            attributes(Pair("Main-Class", "dev.orf1.ApplicationKt"))
        }
    }
}

tasks {
    "build" {
        dependsOn(shadowJar)
    }
}