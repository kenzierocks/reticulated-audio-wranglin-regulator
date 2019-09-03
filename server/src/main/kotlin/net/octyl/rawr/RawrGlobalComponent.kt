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

package net.octyl.rawr

import dagger.Component
import net.octyl.rawr.database.DatabaseBindsModule
import net.octyl.rawr.database.DatabaseModule
import net.octyl.rawr.inject.CoroutineModule
import net.octyl.rawr.inject.EnvironmentModule
import net.octyl.rawr.net.NettyModule
import net.octyl.rawr.net.RawrServer
import net.octyl.rawr.rpc.RpcModule
import net.octyl.rawr.s3.S3Module
import javax.inject.Singleton

@Component(modules = [
    EnvironmentModule::class,
    CoroutineModule::class,
    DatabaseModule::class,
    DatabaseBindsModule::class,
    NettyModule::class,
    RpcModule::class,
    S3Module::class
])
@Singleton
interface RawrGlobalComponent {
    val server: RawrServer
}
