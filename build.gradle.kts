val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val inquirer_version: String by project
val clikt_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.10"
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
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:$inquirer_version")
    implementation("com.github.ajalt.clikt:clikt:$clikt_version")
}

val fatJar = task("fatJar", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    baseName = "${project.name}-fat"
    manifest {
        attributes["Implementation-Title"] = "Cobalt"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "dev.orf1.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}