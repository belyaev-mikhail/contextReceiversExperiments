pluginManagement {
    val localRepository: String by settings
    val kotlinVersion: String by settings
    repositories {
        maven(localRepository)
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version kotlinVersion
    }

}

rootProject.name = "contextReceiversPlusCoroutines"

