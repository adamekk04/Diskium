plugins {
    id("java")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))

    compileOnly(libs.paper.api)
}

tasks.named("build") {
    dependsOn("shadowJar")
}

tasks {
    jar {
        archiveBaseName.set("Diskium-paper")
    }

    shadowJar {
        archiveBaseName.set("Diskium-paper")
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}