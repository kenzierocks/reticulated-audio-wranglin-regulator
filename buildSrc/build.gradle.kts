import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

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

val appKotlin = "1.3.10"
dependencies {
    compile(group = "gradle.plugin.com.techshroom", name = "incise-blue", version = "0.2.1")
    compile(group = "com.android.tools.build", name = "gradle", version = "3.4.0-alpha07")
            .apply {
                // This pulls in 1.3 variants we can't take.
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-common")
                exclude(group = "org.jetbrains.kotlin", module = "kotlin-script-runtime")
            }
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
