import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val versionKotlin: String by project
val versionKtor: String by project
val versionLogback: String by project

plugins {
    application
    kotlin("jvm") version "1.3.70"
}

group = "com.marctatham"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.jetty.EngineMain"
}

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$versionKotlin")

    implementation("io.ktor:ktor-server-jetty:$versionKtor")
    implementation("ch.qos.logback:logback-classic:$versionLogback")

    testImplementation("io.ktor:ktor-server-tests:$versionKtor")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")
