rootProject.name = "concurrent-rdf-graph-kotlin"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion apply false
    }
}

