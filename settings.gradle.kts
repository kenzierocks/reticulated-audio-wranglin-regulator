rootProject.name = "reticulated-audio-wranglin-regulator"

include(":common-protobuf")
include(":common")
include(":server")
include(":client")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Ktor"
            url = uri("https://kotlin.bintray.com/ktor")
        }
        maven {
            name = "Kotlinx"
            url = uri("https://kotlin.bintray.com/kotlinx")
        }
        maven {
            name = "Kotlin EAP"
            url = uri("https://kotlin.bintray.com/kotlin-eap")
        }
    }
}
