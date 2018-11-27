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

import com.google.protobuf.ByteString
import com.google.protobuf.MessageLite
import net.octyl.rawr.gen.protos.ProtoCursorPage
import net.octyl.rawr.gen.protos.ProtoUuid

/**
 * Wrapper interface for [ProtoCursorPage], providing generic support.
 */
data class RawrCursorPage<T : MessageLite>(
        val id: ProtoUuid,
        val content: List<T>
)

fun <T : MessageLite> RawrCursorPage<T>.toProto(): ProtoCursorPage = ProtoCursorPage.newBuilder().apply {
    val rawrPage = this@toProto
    id = rawrPage.id
    rawrPage.content.forEach { addContent(it.toByteString()) }
}.build()

fun <T : MessageLite> ProtoCursorPage.toRawr(decoder: (ByteString) -> T): RawrCursorPage<T> = RawrCursorPage(
        id,
        contentList.map(decoder)
)
