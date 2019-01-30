import com.techshroom.inciseblue.InciseBlueExtension
import com.techshroom.inciseblue.invoke
import net.octyl.JvmKind
import net.octyl.appKotlin
import net.octyl.aptCreator
import net.octyl.dagger
import net.octyl.guava
import net.octyl.jsr305Plus
import net.octyl.jvmSetup
import net.octyl.kotlinCoroutines
import net.octyl.slf4j

plugins {
    `java-library`
}

jvmSetup(JvmKind.KOTLIN, JavaVersion.VERSION_1_8)

dependencies {
    "api"(project(":common-protobuf"))
    "api"(appKotlin("stdlib-jdk8"))
    "implementation"(appKotlin("reflect"))
    "implementation"(appKotlin("compiler"))
    "implementation"(appKotlin("annotation-processing-gradle"))

    "api"(slf4j())

    "compileOnly"(jsr305Plus())

    "api"(guava())

    "api"(kotlinCoroutines("core"))
    "api"(kotlinCoroutines("guava"))

    "api"(dagger())
    "kapt"(dagger("compiler"))

    "api"(aptCreator("annotations"))
    "kapt"(aptCreator("processor"))
}
