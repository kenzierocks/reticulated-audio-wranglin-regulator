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

package net.octyl.rawr.inject

import com.google.common.util.concurrent.ThreadFactoryBuilder
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class CoroutineModule {

    /**
     * Coroutine scope for executing RPC calls.
     */
    private object RawrRpcCoroutineScope : CoroutineScope by CoroutineScope(
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            ThreadFactoryBuilder()
                .setNameFormat("rawr-rpc-handler-%d")
                .setDaemon(false)
                .build()
        ).asCoroutineDispatcher() +
            CoroutineName("RawrRpc") +
            CoroutineExceptionHandler { _, t ->
                if (t !is CancellationException) {
                    t.printStackTrace()
                }
            } +
            SupervisorJob()
    )

    @[Provides Singleton RPC]
    fun provideRpcCoroutineScope(): CoroutineScope = RawrRpcCoroutineScope

}