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

package net.octyl.rawr.database.codecs

import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import net.octyl.rawr.gen.protos.ProtoUuid
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

class MessageCodecProvider(
        private val bsonTypeClassMap: BsonTypeClassMap = BsonTypeClassMap()
) : CodecProvider {
    companion object {
        private val ID_FIELDS = setOf("id", "_id")
    }

    override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
        if (!Message::class.java.isAssignableFrom(clazz)) {
            return null
        }

        // fine, checked above
        @Suppress("UNCHECKED_CAST")
        return mlCodec(registry, clazz as Class<Message>) as Codec<T>?
    }

    private fun mlCodec(registry: CodecRegistry, clazz: Class<Message>): Codec<Message>? {
        val message = clazz.cast(clazz.getDeclaredMethod("getDefaultInstance").invoke(null))
        return when (val key = getIdFieldDescriptor(message)) {
            null -> SimpleMessageCodec(registry, bsonTypeClassMap, clazz, message)
            else -> CollectibleMessageCodec(registry, bsonTypeClassMap, clazz, message, key)
        }
    }

    private fun getIdFieldDescriptor(message: Message): Descriptors.FieldDescriptor? {
        return message.allFields.keys.singleOrNull {
            it.name in ID_FIELDS
                    && it.type == Descriptors.FieldDescriptor.Type.MESSAGE
                    && it.messageType == ProtoUuid.getDescriptor()
        }
    }
}