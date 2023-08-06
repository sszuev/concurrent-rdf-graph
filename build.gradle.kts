plugins {
    kotlin("jvm")
    id("maven-publish")
    signing
}

group = "com.github.sszuev"
version = "1.0.1-SNAPSHOT"

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


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("${project.group}:${project.name}")
                description.set("Thread-safe RDF Graphs")
                url.set("https://github.com/sszuev/concurrent-rdf-graph")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sszuev/concurrent-rdf-graph.git")
                    developerConnection.set("scm:git:ssh://github.com/sszuev/concurrent-rdf-graph.git")
                    url.set("https://github.com/sszuev/concurrent-rdf-graph")
                }
                developers {
                    developer {
                        id.set("sszuev")
                        name.set("Sergei Zuev")
                        email.set("sss.zuev@gmail.com")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/sszuev/${project.name}")
                credentials {
                    username = project.findProperty("gpr.user") as String?
                    password = project.findProperty("gpr.key") as String?
                }
            }
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

signing {
    sign(publishing.publications["maven"])
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByName("signMavenPublication") {
    enabled = project.hasProperty("sign")
}

kotlin {
    jvmToolchain(11)
}