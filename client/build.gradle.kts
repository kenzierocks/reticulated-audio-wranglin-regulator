import com.techshroom.inciseblue.InciseBlueExtension
import com.techshroom.inciseblue.invoke
import net.octyl.JvmKind
import net.octyl.appKotlin
import net.octyl.aptCreator
import net.octyl.dagger
import net.octyl.junit
import net.octyl.jvmSetup
import net.octyl.kotlinCoroutines
import net.octyl.kotlinIo
import net.octyl.kotlinLogging
import net.octyl.logback
import net.octyl.netty
import org.gradle.plugins.ide.idea.model.IdeaModel
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

configure<InciseBlueExtension> {
    jfx {
        jfxVersion = "13.0.1"
        addDependency("base")
        addDependency("graphics")
        addDependency("media")
        addDependency("controls")
        addDependency("fxml")
    }
}

dependencies {
    "implementation"(project(":common"))
    "implementation"(appKotlin("stdlib-jdk8"))

    "implementation"(logback("classic"))
    "implementation"(logback("core"))
    "implementation"(kotlinLogging())

    "implementation"(kotlinIo())

    "implementation"(kotlinCoroutines("core"))
    "implementation"(kotlinCoroutines("guava"))

    "implementation"(netty())

    "implementation"(dagger())
    "kapt"(dagger("compiler"))

    "implementation"(aptCreator("annotations"))
    "kapt"(aptCreator("processor"))

    "testImplementation"(junit("jupiter-api"))
    "testImplementation"(appKotlin("test-junit5"))
    "testRuntime"(junit("jupiter-engine"))
}
