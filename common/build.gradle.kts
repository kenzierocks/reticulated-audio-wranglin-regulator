import com.techshroom.inciseblue.InciseBlueExtension
import com.techshroom.inciseblue.InciseBluePlugin
import com.techshroom.inciseblue.commonLib
import net.octyl.addCommonRepositories
import net.octyl.appKotlin
import net.octyl.jvmSetup
import net.octyl.JvmKind
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.js.translate.context.Namer.kotlin
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    `java-library`
}

jvmSetup(JvmKind.KOTLIN, JavaVersion.VERSION_1_8)

dependencies {
    "api"(project(":common-protobuf"))
    "api"(appKotlin("stdlib-jdk8"))
    "implementation"(appKotlin("reflect"))

    "api"(group = "org.slf4j", name = "slf4j-api", version = "1.7.25")

    "compileOnly"(group = "com.techshroom", name = "jsr305-plus", version = "0.0.1")

    "api"(group = "com.google.guava", name = "guava", version = "27.0-jre")

    commonLib(group = "org.jetbrains.kotlinx", nameBase = "kotlinx-coroutines", version = "1.0.1") {
        "api"(lib("core"))
        "api"(lib("guava"))
    }

    commonLib(group = "com.google.dagger", nameBase = "dagger", version = "2.19") {
        "api"(lib())
        "kapt"(lib("compiler"))
    }

    commonLib(group = "net.octyl.apt-creator", nameBase = "apt-creator", version = "0.1.1") {
        "api"(lib("annotations"))
        "kapt"(lib("processor"))
    }
}
