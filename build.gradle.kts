import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.cloud.tools.gradle.appengine.appyaml.AppEngineAppYamlExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val versionKotlin: String by project
val versionKtor: String by project
val versionLogback: String by project
val versionApp: String by project
val uberJarFileName: String = "ktor-server-$versionApp-with-dependencies.jar"

// The `apply` approach of adding plugins is the older, yet more flexible method of adding a plugin to
// your build. This is the required approach unless your desired plugin is available on Gradle's Plugin Repo.
// Unfortunately the appengine gradle plugin is not available on Gradle's Plugin Repository:
// https://github.com/GoogleCloudPlatform/app-gradle-plugin
buildscript {
    repositories { jcenter() }
    dependencies { classpath("com.google.cloud.tools:appengine-gradle-plugin:2.2.0") }
}
apply {
    plugin("com.google.cloud.tools.appengine")
}

// Note: The `plugins` block is the newer method of applying plugins, but in order to be able to add a plugin
// via this mechanism they must be available on the Gradle Plugin Repository: http://plugins.gradle.org/
// where possible, plugins should be added via this section
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

configure<AppEngineAppYamlExtension> {
    tools {
        // configure the Cloud Sdk tooling
    }

    stage {
        setAppEngineDirectory("appengine")          // where to find the app.yaml
        setArtifact("build/libs/$uberJarFileName")  // where to find the artifact to upload
    }

    deploy {
        projectId = "GCLOUD_CONFIG"
        version = versionApp        // maintain meaningful application versions
        stopPreviousVersion = true  // stop the current version
        promote = true              // & make this the current version
    }
}