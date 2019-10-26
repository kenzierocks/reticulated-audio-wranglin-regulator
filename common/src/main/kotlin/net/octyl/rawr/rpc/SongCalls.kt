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