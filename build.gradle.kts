plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly(libs.paper.api)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}