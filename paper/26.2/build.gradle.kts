plugins {
    id("java")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":common"))
    compileOnly(libs.paper.api.current)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

sourceSets {
    main {
        java.srcDir("../common/src/main/java")
        resources.srcDir("../common/src/main/resources")
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("Diskium-paper-26.2")
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "apiVersion" to "26.2",
            "description" to project.description
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
