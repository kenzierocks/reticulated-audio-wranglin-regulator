package net.octyl.rawr.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
import com.amazonaws.services.s3.model.ListPartsRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PartETag
import com.amazonaws.services.s3.model.UploadPartRequest
import com.amazonaws.util.BinaryUtils
import com.google.common.hash.Hashing
import kotlinx.atomicfu.atomic
import net.octyl.aptcreator.GenerateCreator
import net.octyl.aptcreator.Provided
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.rpc.toJvm
import net.octyl.rawr.rpc.toProtobuf
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Handles a file upload to S3.
 */
@GenerateCreator
class S3FileUpload(
    private val bucket: String,
    folder: String,
    private val objectMetadata: ObjectMetadata,
    @Provided
    private val s3: AmazonS3
) {

    @Volatile
    private var uploadId: String? = null
    private val fileId = UUID.randomUUID().toProtobuf()
    private val filePath = filePath(folder, fileId)
    private val partCounter = atomic(1)
    private var partList = CopyOnWriteArrayList<PartETag>()

    fun start(): String {
        check(uploadId == null) { "Upload already started." }
        return s3.initiateMultipartUpload(InitiateMultipartUploadRequest(
            bucket, filePath, objectMetadata
        )).uploadId.also {
            uploadId = it
        }
    }

    fun upload(byteArray: ByteArray) {
        @Suppress("DEPRECATION")
        val md5 = BinaryUtils.toBase64(Hashing.md5().hashBytes(byteArray).asBytes())
        val num = partCounter.getAndIncrement()
        val upload = s3.uploadPart(UploadPartRequest().apply {
            bucketName = bucket
            key = filePath
            uploadId = this@S3FileUpload.uploadId!!
            md5Digest = md5
            partNumber = num
            partSize = byteArray.size.toLong()
            inputStream = ByteArrayInputStream(byteArray)
        })
        partList.add(PartETag(num, upload.eTag))
    }

    fun abort() {
        // small request to check for parts
        val listPartsRequest = ListPartsRequest(
            bucket, filePath, uploadId!!
        ).withMaxParts(1)
        do {
            try {
                s3.abortMultipartUpload(AbortMultipartUploadRequest(
                    bucket, filePath, uploadId!!
                ))
            } catch (awsEx: AmazonS3Exception) {
                if (awsEx.errorCode != "NoSuchUpload") {
                    throw awsEx
                }
            }
        } while (s3.listParts(listPartsRequest).parts.isNotEmpty())
    }

    fun finish(): ProtoUuid {
        s3.completeMultipartUpload(CompleteMultipartUploadRequest(
            bucket, filePath, uploadId!!, partList.toList()
        ))
        return fileId
    }

}

fun filePath(folder: String, file: ProtoUuid) = "$folder/${file.toJvm()}"
