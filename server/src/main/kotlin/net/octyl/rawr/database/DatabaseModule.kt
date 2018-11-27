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

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Indexes.hashed
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import dagger.Module
import dagger.Provides
import net.octyl.rawr.database.codecs.MessageCodecProvider
import net.octyl.rawr.gen.protos.Song
import net.octyl.rawr.inject.Mongo
import net.octyl.rawr.inject.RawrHost
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideCodecRegistry(): CodecRegistry {
        val localDocumentRegistry = CodecRegistries.fromProviders(
                MessageCodecProvider()
        )
        val defaultRegistry = MongoClientSettings.getDefaultCodecRegistry()
        return CodecRegistries.fromRegistries(
                localDocumentRegistry,
                defaultRegistry
        )
    }

    @Provides
    @Singleton
    fun provideMongoClient(@Mongo addr: RawrHost, codecRegistry: CodecRegistry): MongoClient {
        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(ConnectionString("mongodb://${addr.host}:${addr.port}"))
                .codecRegistry(codecRegistry)
                .build())
    }

    @Provides
    @Singleton
    fun provideMongoDatabase(client: MongoClient): MongoDatabase {
        return client.getDatabase("reticulated-audio-wranglin-regulator")
    }

    @Provides
    @Singleton
    fun provideSongsCollection(database: MongoDatabase): MongoCollection<Song> {
        val coll = database.getTypedCollection<Song>("songs")
        // setup indexes now:
        coll.createIndex(hashed("tags._id"))
        return coll
    }
}