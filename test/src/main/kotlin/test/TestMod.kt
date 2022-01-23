package test

import arc.util.Log
import mindustry.Vars
import mindustry.mod.Mod

@Suppress("unused")
class TestMod : Mod() {
    init {
        Vars.enableConsole = true

        Log.info("$logHeader Loaded test mod successfully.")
    }

    companion object {
        const val logHeader = "[TestMod]"
    }
}