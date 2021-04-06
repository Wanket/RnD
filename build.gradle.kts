import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform") version "1.4.31"
    kotlin("plugin.serialization") version "1.4.31"
    application
}

group = "ru.wanket"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val ktorVersion: String by project
        val kotlinxSerializationVersion: String by project
        val kotlinxDatetimeVersion: String by project

        val commonTest by getting
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${kotlinxDatetimeVersion}")
            }
        }
        val jvmMain by getting {
            val exposedVersion: String by project
            val postgresqlVersion: String by project
            val slf4jLog4j12Version: String by project

            dependencies {
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-sessions:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("org.slf4j:slf4j-log4j12:$slf4jLog4j12Version")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("org.postgresql:postgresql:$postgresqlVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            val reactVersion: String by project
            val styledComponentsVersion: String by project

            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")

                implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
                implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
                implementation(npm("react", reactVersion))
                implementation(npm("react-is", reactVersion))
                implementation(npm("react-dom", reactVersion))

                implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
                implementation(npm("styled-components", styledComponentsVersion))
            }
        }
        val jsTest by getting
    }
}

application {
    mainClassName = "ServerKt"
}

tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    outputFileName = "output.js"
}

tasks.getByName<Jar>("jvmJar") {
    dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")
    from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
}

tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    classpath(tasks.getByName<Jar>("jvmJar"))
}