/*
 * This file is part of reticulated-audio-wranglin-regulator, licensed under the MIT License (MIT).
 *
 * Copyright (c) Kenzie Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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