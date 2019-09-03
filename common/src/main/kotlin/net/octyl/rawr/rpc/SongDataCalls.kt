package net.octyl.rawr.rpc

import net.octyl.rawr.gen.protos.DownloadPacket
import net.octyl.rawr.gen.protos.DownloadRequest
import net.octyl.rawr.gen.protos.ProtoUuid
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
    suspend fun startUpload(): String

    /**
     * Cancel a song upload.
     */
    @RawrCallMarker
    suspend fun cancelUpload(uploadId: String)

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
    suspend fun finishUpload(uploadId: String): ProtoUuid

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