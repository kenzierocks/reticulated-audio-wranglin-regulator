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

import net.octyl.rawr.gen.protos.DownloadPacket
import net.octyl.rawr.gen.protos.DownloadRequest
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.gen.protos.UploadId
import net.octyl.rawr.gen.protos.UploadPacket


/**
 * RPC calls dealing with song data transfer.
 */
interface SongDataCalls {

    /**
     * Start a new song file upload.
     *
     * @return an ID for uploading data via [upload]
     */
    @RawrCallMarker
    suspend fun startUpload(): UploadId

    /**
     * Cancel a song upload.
     */
    @RawrCallMarker
    suspend fun cancelUpload(request: UploadId)

    /**
     * Upload song data.
     */
    @RawrCallMarker
    suspend fun upload(packet: UploadPacket)

    /**
     * Finish an upload, generating a complete file reference.
     *
     * @return the ID for the new file
     */
    @RawrCallMarker
    suspend fun finishUpload(request: UploadId): ProtoUuid

    /**
     * Read from a song file.
     */
    @RawrCallMarker
    suspend fun read(request: DownloadRequest): DownloadPacket

    /**
     * Delete a song file.
     */
    @RawrCallMarker
    suspend fun delete(file: ProtoUuid)

}