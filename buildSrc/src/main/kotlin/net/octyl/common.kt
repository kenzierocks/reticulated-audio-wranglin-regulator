package net.octyl

import com.techshroom.inciseblue.InciseBlueExtension
import net.minecrell.gradle.licenser.LicenseExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

val Project.KOTLIN_VERSION: String
    get() = "1.3.20"

// Kotlin dependency for the actual code, vs the Gradle scripts
fun Project.appKotlin(name: String): String =
        "org.jetbrains.kotlin:kotlin-$name:$KOTLIN_VERSION"

private fun kotlin(name: String) = "org.jetbrains.kotlin.$name"

enum class JvmKind(val wantsKotlin: Boolean,
                   val wantsJavaPlugin: Boolean) {
    PLAIN(false, true),
    KOTLIN(true, false);
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
