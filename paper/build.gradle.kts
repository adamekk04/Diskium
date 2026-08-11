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