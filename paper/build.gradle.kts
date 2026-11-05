import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.language.jvm.tasks.ProcessResources
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    base
    alias(libs.plugins.run.paper) apply false
    alias(libs.plugins.shadow) apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "xyz.jpenilla.run-paper")
    apply(plugin = "com.gradleup.shadow")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        sourceSets.named("main") {
            java.srcDir(rootProject.file("paper/common/src/main/java"))
            resources.srcDir(rootProject.file("paper/common/src/main/resources"))
        }
    }

    afterEvaluate {
        val paperVersion = extensions.extraProperties["paperVersion"] as String

        dependencies {
            "implementation"(project(":common"))
        }

        tasks.named("build") {
            dependsOn("shadowJar")
        }
        tasks.named("jar") {
            enabled = false
        }
        tasks.named<ShadowJar>("shadowJar") {
            archiveBaseName.set("Diskium-paper-$paperVersion")
            archiveClassifier.set("")
        }
        tasks.named<RunServer>("runServer") {
            minecraftVersion(paperVersion)
            jvmArgs("-Xms2G", "-Xmx2G")
        }
        tasks.named<ProcessResources>("processResources") {
            val props = mapOf(
                "version" to project.version,
                "apiVersion" to paperVersion,
                "description" to project.description
            )
            inputs.properties(props)
            filesMatching("paper-plugin.yml") {
                expand(props)
            }
        }
    }
}
