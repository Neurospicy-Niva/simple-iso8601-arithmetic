plugins {
    kotlin("jvm") version "2.0.21"
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "icu.neurospicy"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name = "Simple ISO 8601 Arithmetic"
                description = "A lightweight Kotlin library for parsing and evaluating time arithmetic expressions using ISO 8601 format"
                url = "https://github.com/neurospicy-niva/simple-iso8601-arithmetic"
                
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                
                developers {
                    developer {
                        id = "steineggerroland"
                        name = "Roland Steinegger"
                        email = "roland@neurospicy.icu"
                    }
                }
                
                scm {
                    connection = "scm:git:git://github.com/neurospicy-niva/simple-iso8601-arithmetic.git"
                    developerConnection = "scm:git:ssh://github.com/neurospicy-niva/simple-iso8601-arithmetic.git"
                    url = "https://github.com/neurospicy-niva/simple-iso8601-arithmetic"
                }
            }
        }
    }
    
    repositories {
        maven {
            val releaseRepo = "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://central.sonatype.com/repository/maven-snapshots/"

            name = "sonatype"
            url = uri(if(!project.version.toString().endsWith("SNAPSHOT")) releaseRepo else snapshotRepo)
            credentials {
                username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("SONATYPE_USERNAME")
                password = project.findProperty("sonatypePassword") as String? ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/neurospicy-niva/simple-iso8601-arithmetic")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

nexusPublishing {
    // Endpunkte des Portals (du nutzt sie bereits):
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

tasks.withType<Sign>().configureEach {
    onlyIf { project.findProperty("signingKey") != null }
}

tasks.wrapper {
    gradleVersion = "8.14.2"
    distributionType = Wrapper.DistributionType.BIN
}
