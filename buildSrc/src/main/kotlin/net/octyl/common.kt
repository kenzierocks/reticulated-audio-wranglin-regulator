package net.octyl

import com.techshroom.inciseblue.InciseBlueExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

val KOTLIN_VERSION: String
    get() = "1.3.40"

// Kotlin dependency for the actual code, vs the Gradle scripts
fun appKotlin(name: String): String =
        "org.jetbrains.kotlin:kotlin-$name:$KOTLIN_VERSION"

private fun kotlin(name: String) = "org.jetbrains.kotlin.$name"

enum class JvmKind(val wantsKotlin: Boolean,
                   val wantsJavaPlugin: Boolean) {
    PLAIN(false, true),
    KOTLIN(true, false);
}

fun Project.jvmSetup(jvmKind: JvmKind, javaVersion: JavaVersion, releasePlugin: Boolean = false) {
    apply(plugin = "com.techshroom.incise-blue")
    if (jvmKind.wantsJavaPlugin) {
        apply(plugin = "java")
    }
    if (releasePlugin) {
        apply(plugin = "net.researchgate.release")
    }

    configure<InciseBlueExtension> {
        util(Action {
            it.setJavaVersion(javaVersion.toString())
        })
        ide()
        license()
    }

    addCommonRepositories()

    when (jvmKind) {
        JvmKind.KOTLIN -> kotlinJvmPlainSetup()
        else -> {
        }
    }
    if (jvmKind.wantsKotlin) {
        apply(plugin = kotlin("kapt"))
        configure<KaptExtension> {
            correctErrorTypes = true
            includeCompileClasspath = false
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
