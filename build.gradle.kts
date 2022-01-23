import java.net.URI
import java.io.FileReader
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

val mindustryVersion: String by extra
val kotlinVersion: String by extra

// credential deserialization

val gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

val credentialFile = File("$projectDir/credentials.json")
val credentialReader = FileReader(credentialFile)

val credentials = gson.fromJson(credentialReader, JsonObject::class.java)

val repsy = credentials["repsy"].asJsonObject
val repsyUsername = repsy["username"].asString
val repsyPassword = repsy["password"].asString

// end - credential deserialization

plugins {
    kotlin("jvm") version "1.5.0"
    `java-gradle-plugin`
    `maven-publish`
    `kotlin-dsl`
}

base {
    group = "nichrosia"
    archivesName.set("deploy")
    version = "0.1.1"
}

java {
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven { setUrl("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        create("base") {
            id = "nichrosia.mindeploy"
            implementationClass = "deploy.DeployPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = URI("https://repo.repsy.io/mvn/nichrosia/default")

            credentials {
                username = repsyUsername
                password = repsyPassword
            }
        }
    }
}