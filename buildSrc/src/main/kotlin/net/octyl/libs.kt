package net.octyl

import com.techshroom.inciseblue.commonLib
import org.gradle.api.Project

val Project.slf4j
    get() = commonLib(group = "org.slf4j", nameBase = "slf4j-api", version = "1.7.28")
val Project.kotlinLogging
    get() = commonLib(group = "io.github.microutils", nameBase = "kotlin-logging", version = "1.7.6")
val Project.logback
    get() = commonLib(group = "ch.qos.logback", nameBase = "logback", version = "1.2.3")
val Project.jsr305Plus
    get() = commonLib(group = "com.techshroom", nameBase = "jsr305-plus", version = "0.0.1")
val Project.guava
    get() = commonLib(group = "com.google.guava", nameBase = "guava", version = "28.1-jre")
val Project.aptCreator
    get() = commonLib(group = "net.octyl.apt-creator", nameBase = "apt-creator", version = "0.1.4")
val Project.dagger
    get() = commonLib(group = "com.google.dagger", nameBase = "dagger", version = "2.25.2")
val Project.kotlinIo
    get() = commonLib(group = "org.jetbrains.kotlinx", nameBase = "kotlinx-io-jvm", version = "0.1.15")
val Project.kotlinCoroutines
    get() = commonLib(group = "org.jetbrains.kotlinx", nameBase = "kotlinx-coroutines", version = "1.3.2")
val Project.netty
    get() = commonLib(group = "io.netty", nameBase = "netty-all", version = "4.1.43.Final")
val Project.mongodbDriverReactiveStreams
    get() = commonLib(group = "org.mongodb", nameBase = "mongodb-driver-reactivestreams", version = "1.12.0")
val Project.protobuf
    get() = commonLib("com.google.protobuf", nameBase = "protobuf-java", version = "3.10.0")
val Project.aws
    get() = commonLib(group = "com.amazonaws", nameBase = "aws-java-sdk", version = "1.11.659")
val Project.junit
    get() = commonLib(group = "org.junit.jupiter", nameBase = "junit", version = "5.5.2")
