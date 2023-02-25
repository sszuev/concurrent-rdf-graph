plugins {
    kotlin("jvm")
}

group = "com.github.sszuev"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion: String by project
    val jenaVersion: String by project
    val kotlinCoroutinesVersion: String by project

    implementation("org.apache.jena:jena-arq:$jenaVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}