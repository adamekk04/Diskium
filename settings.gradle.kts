pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "Diskium"

include("common")

// Each platform owns its build configuration and version modules.
providers.gradleProperty("platforms").get().split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .forEach { platform ->
        include(platform)
        file(platform).listFiles()
            ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
            ?.sortedBy { it.name }
            ?.forEach { include("$platform:${it.name}") }
    }
