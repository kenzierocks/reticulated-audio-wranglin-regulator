package net.octyl.rawr.s3

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class S3Module {
    @Provides
    @Singleton
    fun provideS3Client(awsCredentials: AWSCredentialsProvider): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
            .withCredentials(awsCredentials)
            .build()
    }
}