import deploy.DeployExtension

plugins {
    kotlin("jvm") version "1.6.0"
    id("nichrosia.mindeploy") version "0.2"
}

apply(plugin = "nichrosia.mindeploy")

val mindustryVersion: String by extra
val kotlinVersion: String by extra

repositories {
    mavenCentral()
    maven { setUrl("https://www.jitpack.io") }
}

dependencies {
    compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()

        maven { setUrl("https://www.jitpack.io") }
    }

    dependencies {
        classpath("nichrosia:MindustryLive:0.1")
    }
}