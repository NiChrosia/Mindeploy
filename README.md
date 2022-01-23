# Mindeploy

A simple gradle plugin for compiling & running Mindustry mods.

## Usage

### Buildscript

```kotlin
// settings.gradle(.kts)
pluginManagement {
    repositories {
        gradlePluginPortal()
        
        maven { setUrl("https://repsy.io/mvn/nichrosia/default/") }
    }
}

// build.gradle(.kts)
plugins {
    id("nichrosia.mindeploy") version "0.1.1"
}
```

### Compiling

Compiling mods is done via the gradle task `deployMod`, and `deployDexedMod` for android compatibility.

### Running

Running your mod is done by the gradle task `runMindustry`. Do note that this requires having `mindustryVersion` set in your gradle.properties, as a mindustry tag, i.e. v135.