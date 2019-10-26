plugins {
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    gradlePluginPortal()
    jcenter()
    google()
    maven {
        name = "KotlinX"
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
    maven {
        name = "Ktor"
        url = uri("https://kotlin.bintray.com/ktor")
    }
    maven {
        name = "Kotlin EAP"
        url = uri("https://kotlin.bintray.com/kotlin-eap")
    }
}

val appKotlin = "1.3.50"
dependencies {
    compile(group = "gradle.plugin.com.techshroom", name = "incise-blue", version = "0.5.3")
    compile(group = "gradle.plugin.net.minecrell", name = "licenser", version = "0.4.1")
    compile(group = "net.researchgate", name = "gradle-release", version = "2.8.1")
    compile(gradleKotlinDsl())
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile(kotlin("gradle-plugin", version = appKotlin))
            .let { it as ExternalModuleDependency }
            .apply {
                // This pulls in 1.3 variants we can't take.
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-script-runtime")
            }
    runtime(kotlin("serialization", version = appKotlin))
}
