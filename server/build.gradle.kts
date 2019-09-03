import com.techshroom.inciseblue.invoke
import net.octyl.JvmKind
import net.octyl.appKotlin
import net.octyl.aptCreator
import net.octyl.aws
import net.octyl.dagger
import net.octyl.jsr305Plus
import net.octyl.jvmSetup
import net.octyl.kotlinCoroutines
import net.octyl.kotlinIo
import net.octyl.kotlinLogging
import net.octyl.logback
import net.octyl.mongodbDriverReactiveStreams
import net.octyl.netty
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile

jvmSetup(JvmKind.KOTLIN, JavaVersion.VERSION_12)

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

    "implementation"(logback("classic"))
    "implementation"(logback("core"))
    "implementation"(kotlinLogging())

    "implementation"(aws("s3"))

    "compileOnly"(jsr305Plus())

    "implementation"(kotlinIo())

    "implementation"(kotlinCoroutines("core"))
    "implementation"(kotlinCoroutines("guava"))
    "implementation"(kotlinCoroutines("reactive"))

    "implementation"(netty())

    "implementation"(dagger())
    "kapt"(dagger("compiler"))

    "implementation"(aptCreator("annotations"))
    "kapt"(aptCreator("processor"))

    "implementation"(mongodbDriverReactiveStreams())
}
