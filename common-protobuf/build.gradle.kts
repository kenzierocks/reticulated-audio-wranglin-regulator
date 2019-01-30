import com.google.protobuf.gradle.ExecutableLocator
import com.google.protobuf.gradle.ProtobufConfigurator
import com.google.protobuf.gradle.ProtobufConvention
import com.techshroom.inciseblue.invoke
import net.octyl.JvmKind
import net.octyl.jvmSetup
import net.octyl.protobuf

plugins {
    id("com.google.protobuf") version "0.8.8"
    `java-library`
}

jvmSetup(JvmKind.PLAIN, JavaVersion.VERSION_1_8)

val protobufVersion = protobuf().version!!
dependencies {
    "api"(protobuf())
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
