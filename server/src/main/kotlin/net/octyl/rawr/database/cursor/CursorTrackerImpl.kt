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

package net.octyl.rawr.database.cursor

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.protobuf.MessageLite
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.produceIn
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.gen.protos.RawrCursorRequest
import net.octyl.rawr.rpc.RawrCursorPage
import net.octyl.rawr.rpc.toProtobuf
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CursorTrackerImpl @Inject constructor() : CursorTracker {

    private class CursorData(
        val id: ProtoUuid,
        val channel: ReceiveChannel<*>
    ) {
        var expireFuture: ScheduledFuture<*>? = null
    }

    private fun CursorData.onUsed() {
        val ef = expireFuture
        if (ef != null) {
            if (!ef.cancel(true)) {
                // we're already dead, too late
                return
            }
            expireFuture = null
        }
        expireFuture = cursorExecutor.schedule({
            removeCursor(this)
        }, 1L, TimeUnit.MINUTES)
    }

    private val cursorExecutor = Executors.newSingleThreadScheduledExecutor(
            ThreadFactoryBuilder()
                    .setDaemon(true)
                .setNameFormat("cursor-tracker-%d")
                    .build()
    )
    private val coroutineScope = CoroutineScope(cursorExecutor.asCoroutineDispatcher() +
        CoroutineName("cursor-tracker"))
    private val cursors = mutableMapOf<ProtoUuid, CursorData>()

    private fun removeCursor(cursorData: CursorData) {
        cursors.remove(cursorData.id)
        cursorData.channel.cancel(CancellationException("Cursor removed"))
    }

    @UseExperimental(FlowPreview::class)
    override suspend fun <T : MessageLite> addCursor(flow: Flow<T>): ProtoUuid {
        val uuid = UUID.randomUUID().toProtobuf()
        // copy the flow to a (limited) channel
        val cursorChannel = flow.produceIn(coroutineScope)
        cursors[uuid] = CursorData(uuid, cursorChannel)
        return uuid
    }

    override suspend fun <T : MessageLite> onCursorRequest(cursorRequest: RawrCursorRequest): RawrCursorPage<T> {
        val cursor = cursors[cursorRequest.id] ?: throw InvalidCursorException(cursorRequest.id)
        // refresh before we start
        cursor.onUsed()
        // this should be good, unless someone mixes IDs, but then the 500 is their fault :)
        @Suppress("UNCHECKED_CAST")
        val iterator = cursor.channel.iterator() as ChannelIterator<T>
        val results = ArrayList<T>(cursorRequest.maxSize)
        while (results.size < cursorRequest.maxSize && iterator.hasNext()) {
            results.add(iterator.next())
        }
        if (!iterator.hasNext()) {
            cursors.remove(cursorRequest.id)
        }
        return RawrCursorPage(cursorRequest.id, results)
    }

    override fun close() {
        cursorExecutor.shutdown()
        val cursorList = cursors.values.toList()
        cursorList.forEach { removeCursor(it) }
    }

}