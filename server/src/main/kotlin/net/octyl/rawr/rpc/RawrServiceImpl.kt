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

package net.octyl.rawr.rpc

import com.amazonaws.services.s3.model.ObjectMetadata
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import net.octyl.rawr.database.Database
import net.octyl.rawr.database.cursor.CursorTracker
import net.octyl.rawr.database.cursor.autoAddCursor
import net.octyl.rawr.gen.protos.DownloadPacket
import net.octyl.rawr.gen.protos.DownloadRequest
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.gen.protos.ProtoUuidList
import net.octyl.rawr.gen.protos.RawrCursorRequest
import net.octyl.rawr.gen.protos.Song
import net.octyl.rawr.gen.protos.TagList
import net.octyl.rawr.gen.protos.UploadPacket
import net.octyl.rawr.s3.RawrS3Storage
import net.octyl.rawr.s3.S3Files
import javax.inject.Inject

@UseExperimental(ExperimentalCoroutinesApi::class)
class RawrServiceImpl @Inject constructor(
    private val database: Database,
    private val cursorTracker: CursorTracker,
    private val s3Files: S3Files,
    private val s3Storage: RawrS3Storage
) : RawrService {

    override suspend fun startUpload(): String {
        return withContext(Dispatchers.IO) {
            s3Files.startUpload(s3Storage.bucket, s3Storage.folder, ObjectMetadata())
        }
    }

    override suspend fun cancelUpload(uploadId: String) {
        withContext(Dispatchers.IO) {
            s3Files.getUpload(uploadId)?.abort()
        }
    }

    override suspend fun upload(packet: UploadPacket) {
        withContext(Dispatchers.IO) {
            s3Files.requireUpload(packet.id).upload(packet.content.toByteArray())
        }
    }

    override suspend fun finishUpload(uploadId: String): ProtoUuid {
        return withContext(Dispatchers.IO) {s3Files.requireUpload(uploadId).finish()}
    }

    override suspend fun read(request: DownloadRequest): DownloadPacket {
        return withContext(Dispatchers.IO) {
            DownloadPacket.newBuilder()
                .setContent(ByteString.copyFrom(s3Files.read(
                    s3Storage.bucket, s3Storage.folder,
                    request.id, request.offset, request.size.toLong()
                )))
                .build()
        }
    }

    override suspend fun delete(file: ProtoUuid) {
        withContext(Dispatchers.IO) {
            s3Files.delete(s3Storage.bucket, s3Storage.folder, file)
        }
    }

    override suspend fun addSong(song: Song) {
        database.addSong(song)
    }

    override suspend fun getSongs(songIds: ProtoUuidList,
                                  cursorRequest: RawrCursorRequest): RawrCursorPage<Song> {
        return cursorTracker.autoAddCursor(cursorRequest) { database.getSongs(songIds.idsList) }
    }

    override suspend fun findTaggedSongs(tags: TagList,
                                         cursorRequest: RawrCursorRequest): RawrCursorPage<Song> {
        return cursorTracker.autoAddCursor(cursorRequest) { database.findTaggedSongs(tags.tagsList) }
    }

}
