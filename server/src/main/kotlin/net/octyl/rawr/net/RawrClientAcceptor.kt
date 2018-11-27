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

package net.octyl.rawr.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import net.octyl.rawr.gen.protos.RawrCall
import net.octyl.rawr.inject.DebugEnabled
import javax.inject.Inject

class RawrClientAcceptor @Inject constructor(
        @DebugEnabled private val debugEnabled: Boolean,
        private val rpcHandlerCreator: RawrRpcHandlerCreator
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val p = ch.pipeline()
        if (debugEnabled) {
            p.addFirst("logger", LoggingHandler(LogLevel.INFO))
        }
        p.addLast("frameDecoder", LengthFieldBasedFrameDecoder(
                1024 * 1024, 0, 4,
                0, 4))
        p.addLast("callDecoder", ProtobufDecoder(RawrCall.getDefaultInstance()))

        p.addLast("frameEncoder", LengthFieldPrepender(4))
        p.addLast("responseEncoder", ProtobufEncoder())

        p.addLast("rpcCaller", rpcHandlerCreator.create())
    }
}