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

package net.octyl.rawr.database

import com.mongodb.DuplicateKeyException
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Filters.all
import com.mongodb.client.model.Filters.gte
import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.openSubscription
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.gen.protos.Song
import net.octyl.rawr.gen.protos.Tag
import org.bson.Document
import javax.inject.Inject

// we use #openSubscription, no other option right now.
@UseExperimental(
        ObsoleteCoroutinesApi::class
)
class MongoDbDatabase @Inject constructor(
        private val songCollection: MongoCollection<Song>
) : Database {

    override suspend fun addSong(song: Song) {
        try {
            songCollection.insertOne(song).awaitFirst()
        } catch (e: DuplicateKeyException) {
            throw SongAlreadyExists(song.id)
        }
    }

    override suspend fun getSongs(ids: List<ProtoUuid>): Flow<Song> {
        if (ids.isEmpty()) {
            return songCollection.find().asFlow()
        }
        return songCollection
                .find(`in`("id", ids))
                .asFlow()
    }

    override suspend fun findTaggedSongs(tags: List<Tag>): Flow<Song> {
        return songCollection
                .find(all(
                        "tags",
                        tags.map {
                            Document("\$elemMatch", it)
                        }
                ))
                .asFlow()
    }
}