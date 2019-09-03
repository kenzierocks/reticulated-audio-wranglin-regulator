package net.octyl.rawr.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.rpc.toJvm
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3Files @Inject constructor(
    private val s3: AmazonS3,
    private val s3FileCreator: S3FileUploadCreator
) {

    private val uploads: MutableMap<String, S3FileUpload> = ConcurrentHashMap()

    fun getUpload(uploadId: String) = uploads[uploadId]

    fun requireUpload(uploadId: String): S3FileUpload =
        requireNotNull(getUpload(uploadId)) { "No upload with ID `$uploadId`" }

    fun startUpload(bucket: String, folder: String, objectMetadata: ObjectMetadata): String {
        val upload = s3FileCreator.create(bucket, folder, objectMetadata)
        return upload.start().also {
            uploads[it] = upload
        }
    }

    fun read(bucket: String, folder: String, item: ProtoUuid, offset: Long, size: Long) : ByteArray {
        val content = s3.getObject(GetObjectRequest(
            bucket, filePath(folder, item)
        ).withRange(offset, offset + size))
        return content.objectContent.use { it.readAllBytes() }
    }

    fun delete(bucket: String, folder: String, item: ProtoUuid) {
        s3.deleteObject(bucket, filePath(folder, item))
    }

}