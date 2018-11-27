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

import com.google.common.base.Strings
import dagger.Module
import dagger.Provides
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

private fun requireEnv(name: String): String {
    return env(name) ?: throw IllegalStateException("Missing required environment variable: `$name`")
}

private fun env(name: String): String? {
    val value: String? = System.getenv(name)
    return Strings.emptyToNull(value?.trim())
}

private fun prop(name: String): String? {
    val value: String? = System.getProperty(name)
    return Strings.emptyToNull(value?.trim())
}

private fun portValue(name: String): Int? {
    val value = prop(name)?.toIntOrNull() ?: return null
    return when {
        value <= 0 -> null
        else -> value
    }
}

data class RawrHost(val host: String, val port: Int) {
    fun toInetSocketAddress() = InetSocketAddress(host, port)
}

@Module
class EnvironmentModule {
    private val databasePath = Paths.get(env("RAWR_DATABASE_PATH") ?: "./rawr-db/")
    private val debugEnabled = env("ENVIRONMENT") == "DEV"
    private val ipAddress = prop("net.octyl.rawr.ip") ?: "localhost"
    private val port = portValue("net.octyl.rawr.port") ?: 7027
    private val mongoHost = prop("net.octyl.rawr.mongodb.ip") ?: "localhost"
    private val mongoPort = portValue("net.octyl.rawr.mongodb.port") ?: 27017

    init {
        if (Files.notExists(databasePath)) {
            Files.createDirectories(databasePath)
        }
    }

    @Provides
    @DatabasePath
    fun provideDatabasePath(): Path {
        return databasePath
    }

    @Provides
    @DebugEnabled
    fun provideDebugEnabled() = debugEnabled

    @Provides
    fun provideRawrHost() = RawrHost(ipAddress, port)

    @Provides
    @Mongo
    fun provideMongoRawrHost() = RawrHost(mongoHost, mongoPort)
}