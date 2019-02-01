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

jvmSetup(JvmKind.KOTLIN, JavaVersion.VERSION_1_8, true)

dependencies {
    "api"(project(":common-protobuf"))
    "api"(appKotlin("stdlib-jdk8"))
    "implementation"(appKotlin("reflect"))

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

configure<InciseBlueExtension> {
    maven {
        projectDescription = "RAWR Music Player common library."
        coords("kenzierocks", "reticulated-audio-wranglin-regulator")
    }
}
