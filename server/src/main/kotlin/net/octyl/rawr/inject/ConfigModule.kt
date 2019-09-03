package net.octyl.rawr.inject

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import dagger.Module
import dagger.Provides
import net.octyl.rawr.s3.RawrS3Storage
import java.util.Properties
import javax.inject.Singleton

@Module
class ConfigModule {

    private val configProperties = Properties().also { props ->
        val stream = requireNotNull(
            javaClass.classLoader.getResourceAsStream("rawr/config.properties")
        ) { "No `rawr/config.properties` resource found." }
        stream.bufferedReader().use {
            props.load(it)
        }
    }

    private fun require(
        name: String,
        message: String = "Required property missing: `$name`",
        filter: (String) -> Boolean = { it.isNotBlank() }
    ): String {
        return requireNotNull(configProperties.getProperty(name).takeIf(filter)) {
            message
        }
    }

    private fun String.asPort(): Int {
        return requireNotNull(toIntOrNull()?.takeIf { it > 0 }) {
            "Invalid port: $this"
        }
    }

    private val host = require("bind.host")
    private val port = require("bind.port").asPort()
    @[Provides Singleton]
    fun provideRawrHost() = RawrHost(host, port)

    private val mongoHost = require("mongo.host")
    private val mongoPort = require("mongo.port").asPort()
    @[Provides Singleton Mongo]
    fun provideMongoRawrHost() = RawrHost(mongoHost, mongoPort)

    private val s3Bucket = require("s3.bucket")
    private val s3Folder = require("s3.folder")

    @[Provides Singleton]
    fun provideS3Storage() = RawrS3Storage(s3Bucket, s3Folder)

    private val awsKeyId = configProperties.getProperty("aws.key.id")
    private val awsKeySecret = configProperties.getProperty("aws.key.secret")

    @[Provides Singleton]
    fun provideAwsCredentials(): AWSCredentialsProvider =
        AWSStaticCredentialsProvider(
            AWSCredentialsProviderChain(
                AWSStaticCredentialsProvider(object : AWSCredentials {
                    override fun getAWSAccessKeyId() = awsKeyId
                    override fun getAWSSecretKey() = awsKeySecret
                }),
                EnvironmentVariableCredentialsProvider()
            ).credentials
        )

}