plugins {
    id("java")
    alias(libs.plugins.run.paper)
}

dependencies {
    implementation(project(":common"))

    compileOnly(libs.paper.api)
}