package net.octyl.rawr.rpc

import net.octyl.rawr.gen.protos.ProtoUuidList
import net.octyl.rawr.gen.protos.RawrCursorRequest
import net.octyl.rawr.gen.protos.Song
import net.octyl.rawr.gen.protos.TagList

interface SongCalls {

    /**
     * Add a song to the database.
     *
     * The ID used in the argument is the file ID given by the upload service.
     */
    @RawrCallMarker
    suspend fun addSong(song: Song)

    /**
     * Retrieve all songs given by the IDs in the list.
     */
    @RawrCallMarker
    suspend fun getSongs(songIds: ProtoUuidList, cursorRequest: RawrCursorRequest): RawrCursorPage<Song>

    /**
     * Retrieve all songs tagged with the given values.
     */
    @RawrCallMarker
    suspend fun findTaggedSongs(tags: TagList, cursorRequest: RawrCursorRequest): RawrCursorPage<Song>

}