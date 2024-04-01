rootProject.name = "concurrent-rdf-graph"

pluginManagement {
    plugins {
        val kotlinVersion: String by settings
        val jmhGradlePluginVersion: String by settings

        kotlin("jvm") version kotlinVersion apply false
        id("me.champeau.jmh") version jmhGradlePluginVersion apply false
    }
}

