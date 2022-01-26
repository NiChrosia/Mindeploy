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
    id("nichrosia.mindeploy") version "0.3"
}
```

### Compiling

Compiling mods is done via the gradle task `deployMod`, and `deployDexedMod` for android compatibility.

### Running

Running your mod is done by the gradle task `runMindustry`. Do note that this requires having `mindustryVersion` set in your gradle.properties, as a mindustry tag, i.e. v135.

Mindeploy automatically generates a `run/` directory to store the Mindustry instance, allowing for easy modification & the ability to use multiple different versions (across your device.)
(The way this works is by setting the `MINDUSTRY_DATA_DIR` environment variable in the `runMindustry` task.)

#### Plugins

Due to a weird bug with IntelliJ (input in run/debug is disabled), a custom run configuration must be created to run a server. To do such, open up IntelliJ's run configuration screen,
and add a shell script. Make the run directory `run/modded/server/`, toggle 'Script file' to 'Script text', and insert '../../../build/libs/Mindustry-Server-version.jar',
with version being the desired Mindustry tag specified in `gradle.properties`. This will eventually be automated by dynamic injection into `.idea/workspace.xml`.