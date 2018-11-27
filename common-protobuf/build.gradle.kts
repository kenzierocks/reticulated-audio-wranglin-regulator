import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufConfigurator
import com.google.protobuf.gradle.ProtobufConvention
import com.google.protobuf.gradle.ProtobufSourceDirectorySet
import com.techshroom.inciseblue.InciseBlueExtension
import com.techshroom.inciseblue.InciseBluePlugin
import com.techshroom.inciseblue.commonLib
import groovy.lang.Closure
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
    id("com.google.protobuf") version "0.8.7"
    `java-library`
}

jvmSetup(JvmKind.PLAIN, JavaVersion.VERSION_1_8)

val protobufVersion = "3.6.1"
dependencies {
    "api"("com.google.protobuf:protobuf-java:$protobufVersion")
}

// make it "nice" for now:
fun ProtobufConvention.protobuf(block: ProtobufConfigurator.() -> Unit) =
        protobuf(KotlinClosure1(block, project, project))

fun ProtobufConfigurator.protoc(block: ExecutableLocator.() -> Unit) =
        protoc(KotlinClosure1(block, project, project))

fun ProtobufConfigurator.plugins(block: NamedDomainObjectContainer<ExecutableLocator>.() -> Unit) =
        plugins(KotlinClosure1(block, project, project))

fun ProtobufConfigurator.generateProtoTasks(block: ProtobufConfigurator.JavaGenerateProtoTaskCollection.() -> Unit) =
        generateProtoTasks(KotlinClosure1(block, project, project))

configure<ProtobufConvention> {
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
//
//        plugins {
//            create("javalite") {
//                artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
//            }
//        }

//        generateProtoTasks {
//            all().forEach { task ->
//                task.builtins.removeIf({ it.name == "java" })
//                task.plugins.create("javalite")
//            }
//        }
    }
}

// don't let the generated compile task spit warnings...
tasks.withType<JavaCompile>().named("compileJava").configure {
    options.isWarnings = false
}

plugins.withType<IdeaPlugin>().configureEach {
    val pbuf = convention.getPlugin<ProtobufConvention>()
    model.module.generatedSourceDirs.add(file("${pbuf.protobuf.generatedFilesBaseDir}/main/java"))
}
