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
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile
import org.jetbrains.kotlin.js.translate.context.Namer.kotlin
import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

jvmSetup(JvmKind.KOTLIN, JavaVersion.VERSION_11)

tasks.withType<AbstractKotlinCompile<K2JVMCompilerArguments>> {
    incremental = false
}

tasks.withType<KotlinJvmCompile> {
    this.kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
}

dependencies {
    "implementation"(project(":common"))
    "implementation"(appKotlin("stdlib-jdk8"))
    "implementation"(appKotlin("reflect"))

    commonLib(group = "ch.qos.logback", nameBase = "logback", version = "1.2.3") {
        "implementation"(lib("classic"))
        "implementation"(lib("core"))
    }

    "compileOnly"(group = "com.techshroom", name = "jsr305-plus", version = "0.0.1")

    commonLib(group = "org.jetbrains.kotlinx", nameBase = "kotlinx-coroutines", version = "1.0.1") {
        "implementation"(lib("core"))
        "implementation"(lib("guava"))
        "implementation"(lib("reactive"))
    }

    "implementation"(group = "io.netty", name = "netty-all", version = "4.1.31.Final")

    commonLib(group = "com.google.dagger", nameBase = "dagger", version = "2.19") {
        "implementation"(lib())
        "kapt"(lib("compiler"))
    }

    commonLib(group = "net.octyl.apt-creator", nameBase = "apt-creator", version = "0.1.1") {
        "implementation"(lib("annotations"))
        "kapt"(lib("processor"))
    }

    "implementation"(group = "org.mongodb", name = "mongodb-driver-reactivestreams", version = "1.10.0")
}
