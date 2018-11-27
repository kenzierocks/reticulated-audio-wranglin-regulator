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

import com.google.protobuf.MessageLite
import kotlinx.coroutines.channels.ReceiveChannel
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.gen.protos.RawrCursorRequest
import net.octyl.rawr.rpc.RawrCursorPage

/**
 * Helper interface for housekeeping of cursor execution.
 */
interface CursorTracker {

    /**
     * Add a cursor call to be tracked.
     */
    suspend fun <T : MessageLite> addCursor(cursorChannel: ReceiveChannel<T>): ProtoUuid

    /**
     * Get the next page for the given cursor.
     */
    suspend fun <T : MessageLite> onCursorRequest(cursorRequest: RawrCursorRequest): RawrCursorPage<T>

}

suspend inline fun <T : MessageLite> CursorTracker.autoAddCursor(
        cursorRequest: RawrCursorRequest,
        channelProvider: () -> ReceiveChannel<T>
): RawrCursorPage<T> {
    val request = when {
        cursorRequest.hasId() -> cursorRequest
        else -> addCursorToRequest(cursorRequest, channelProvider())
    }

    return onCursorRequest(request)
}

suspend inline fun <T : MessageLite> CursorTracker.addCursorToRequest(
        cursorRequest: RawrCursorRequest,
        channelProvider: ReceiveChannel<T>
): RawrCursorRequest {
    val cursorUuid = addCursor(channelProvider)

    return cursorRequest.toBuilder()
            .setId(cursorUuid)
            .build()
}
