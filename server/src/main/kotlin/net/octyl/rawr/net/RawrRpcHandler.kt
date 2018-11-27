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

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.octyl.aptcreator.GenerateCreator
import net.octyl.aptcreator.Provided
import net.octyl.rawr.gen.protos.RawrCall
import net.octyl.rawr.inject.RPC
import net.octyl.rawr.rpc.RawrService
import net.octyl.rawr.rpc.call

@GenerateCreator
class RawrRpcHandler constructor(
        @Provided @RPC private val appCoroutineScope: CoroutineScope,
        @Provided private val rawrService: RawrService
) : SimpleChannelInboundHandler<RawrCall>() {
    override fun channelRead0(ctx: ChannelHandlerContext, context: RawrCall) {
        // launch 100% async -- no need to hold threads in netty, we'll call it
        // from the app threads and that's 100% safe!
        appCoroutineScope.launch {
            rawrService.call(context)
        }
    }
}