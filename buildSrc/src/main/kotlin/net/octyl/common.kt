package net.octyl

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.techshroom.inciseblue.InciseBlueExtension
import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

val Project.KOTLIN_VERSION: String
    get() = "1.3.10"

// Kotlin dependency for the actual code, vs the Gradle scripts
fun Project.appKotlin(name: String): String =
        "org.jetbrains.kotlin:kotlin-$name:$KOTLIN_VERSION"

private fun kotlin(name: String) = "org.jetbrains.kotlin.$name"

enum class JvmKind(val wantsKotlin: Boolean,
                   val wantsJavaPlugin: Boolean) {
    PLAIN(false, true),
    KOTLIN(true, false),
    KOTLIN_ANDROID(true, false);
}

fun Project.jvmSetup(jvmKind: JvmKind, javaVersion: JavaVersion) {
    apply(plugin = "com.techshroom.incise-blue")
    if (jvmKind.wantsJavaPlugin) {
        apply(plugin = "java")
    }

    configure<InciseBlueExtension> {
        util(Action {
            it.setJavaVersion(javaVersion.toString())
        })
        ide()
        license()
    }

    configure<LicenseExtension>() {
        include("**/*.kt")
    }

    addCommonRepositories()

    when (jvmKind) {
        JvmKind.KOTLIN_ANDROID -> kotlinAndroidSetup()
        JvmKind.KOTLIN -> kotlinJvmPlainSetup()
        else -> {
        }
    }
    if (jvmKind.wantsKotlin) {
        apply(plugin = kotlin("kapt"))
        tasks.withType<KotlinJvmCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        configure<KaptExtension> {
            correctErrorTypes = true
        }
    }
}

private fun Project.kotlinAndroidSetup() {
    apply(plugin = "com.android.application")
    apply(plugin = kotlin("android"))
    apply(plugin = "kotlin-android-extensions")

    repositories {
        google()
    }

    configure<AppExtension> {
        compileSdkVersion(28)
        defaultConfig {
            it.applicationId = "net.octyl.rawr"
            it.minSdkVersion(24)
            it.targetSdkVersion(28)
        }

        buildTypes {
            it.named("release").configure { release ->
                release.isMinifyEnabled = false
                release.proguardFiles(
                        getDefaultProguardFile("proguard-android.txt"),
                        "proguard-rules.txt")
            }
        }

        sourceSets {
            it.named("main").configure {
                it.java.srcDirs(file("src/main/kotlin"))
            }
        }
    }
}

private fun Project.kotlinJvmPlainSetup() {
    apply(plugin = kotlin("jvm"))
}

fun Project.addCommonRepositories() {
    repositories.run {
        jcenter()
        maven {
            it.run {
                name = "KotlinX"
                url = uri("https://kotlin.bintray.com/kotlinx")
            }
        }
        maven {
            it.run {
                name = "Kotlin EAP"
                url = uri("https://kotlin.bintray.com/kotlin-eap")
            }
        }
    }
}
