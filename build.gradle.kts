import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val versionKotlin: String by project
val versionKtor: String by project
val versionLogback: String by project
val versionApp: String by project
val uberJarFileName: String = "ktor-server-$versionApp-with-dependencies.jar"

plugins {
    application
    kotlin("jvm") version "1.3.70"

    // Shadow plugin - enable support for building our UberJar
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.marctatham"
version = versionApp
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

// Configure the "shadowJar" task to properly build our UberJar
// we effectively want a jar with zero dependencies we can run and will "just work"
tasks {
    named<ShadowJar>("shadowJar") {
        // explicitly configure the filename of the resulting UberJar
        archiveFileName.set(uberJarFileName)

        // Appends entries in META-INF/services resources into a single resource. For example, if there are several
        // META-INF/services/org.apache.maven.project.ProjectBuilder resources spread across many JARs the individual
        // entries will all be concatenated into a single META-INF/services/org.apache.maven.project.ProjectBuilder
        // resource packaged into the resultant JAR produced by the shading process -
        // Effectively ensures we bring along all the necessary bits from Jetty
        mergeServiceFiles()

        // As per the App Engine java11 standard environment requirements listed here:
        // https://cloud.google.com/appengine/docs/standard/java11/runtime
        // Your Jar must contain a Main-Class entry in its META-INF/MANIFEST.MF metadata file
        manifest {
            attributes(mapOf("Main-Class" to application.mainClassName))
        }
    }
}