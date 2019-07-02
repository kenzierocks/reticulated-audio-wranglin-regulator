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

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.BOOLEAN
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.BYTE_STRING
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.DOUBLE
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.ENUM
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.FLOAT
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.INT
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.LONG
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.MESSAGE
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType.STRING
import com.google.protobuf.Message
import net.octyl.rawr.gen.protos.ProtoUuid
import net.octyl.rawr.rpc.toProtobuf
import org.bson.BsonBinarySubType
import org.bson.BsonDocument
import org.bson.BsonInt64
import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.BsonTypeCodecMap
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import java.util.UUID

sealed class MessageCodec(
        private val codecRegistry: CodecRegistry,
        bsonTypeClassMap: BsonTypeClassMap,
        private val encoderClass: Class<Message>,
        private val defaultMessage: Message
) : Codec<Message> {

    private val bsonTypeCodecMap = BsonTypeCodecMap(bsonTypeClassMap, codecRegistry)
    private val reverseFieldMap = defaultMessage.allFields.keys
            .map { it.name to it }
            .toMap()

    protected open val Descriptors.FieldDescriptor.mongoDbName: String
        get() = name

    override fun getEncoderClass() = encoderClass

    override fun encode(writer: BsonWriter, value: Message, encoderContext: EncoderContext) {
        writer.writeMapLike(encoderContext) { cb ->
            value.allFields.forEach { (k, v) ->
                if (k.hasDefaultValue() && k.defaultValue === v) {
                    // no need to serialize this one
                    return@forEach
                }
                cb(k.name, v)
            }
        }
    }

    private fun BsonWriter.writeValue(v: Any?, encoderContext: EncoderContext) {
        when (v) {
            is Iterable<*> -> writeIterable(v, encoderContext)
            is Map<*, *> -> writeMapLike(encoderContext) { cb ->
                v.forEach { (any, u) ->
                    cb(any.toString(), u)
                }
            }
            null -> writeNull()
            else -> encoderContext.encodeWithChildContext(codecRegistry[v.javaClass], this, v)
        }
    }

    private fun BsonWriter.writeIterable(v: Iterable<*>, encoderContext: EncoderContext) {
        writeStartArray()
        v.forEach {
            writeValue(it, encoderContext)
        }
        writeEndArray()
    }

    private inline fun BsonWriter.writeMapLike(encoderContext: EncoderContext,
                                               iterateCall: ((String, Any?) -> Unit) -> Unit) {
        writeStartDocument()
        iterateCall { k, v ->
            writeName(k)
            writeValue(v, encoderContext)
        }
        writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Message {
        val message = defaultMessage.newBuilderForType()
        reader.readMapLike(decoderContext) { k, v ->
            reverseFieldMap[k]?.apply {
                message.setField(this, v)
            }
        }
        return message.build()
    }

    private fun BsonReader.readValue(key: String, decoderContext: DecoderContext): Any? {
        return when (val fieldDescr = reverseFieldMap[key]) {
            null -> readUnknownValue(decoderContext)
            else -> readFieldDescriptor(fieldDescr, decoderContext)
        }
    }

    private fun BsonReader.readFieldDescriptor(fieldDescriptor: Descriptors.FieldDescriptor,
                                               decoderContext: DecoderContext): Any? {
        return when (fieldDescriptor.javaType) {
            null -> throw IllegalStateException("Null not permitted.")
            INT, ENUM -> readInt32()
            LONG -> readInt64()
            FLOAT -> readDouble().toFloat()
            BOOLEAN -> readBoolean()
            DOUBLE -> readDouble()
            STRING -> readString()
            BYTE_STRING -> ByteString.copyFrom(readBinaryData().data)
            MESSAGE -> {
                // get the actual message type, decode into it
                val valueMessage = defaultMessage.getField(fieldDescriptor) as Message
                val codec = codecRegistry[valueMessage.javaClass]
                return decoderContext.decodeWithChildContext(codec, this)
            }
        }
    }

    private fun BsonReader.readUnknownValue(decoderContext: DecoderContext): Any? {
        when (currentBsonType) {
            BsonType.NULL -> {
                readNull()
                return null
            }
            BsonType.ARRAY ->
                return readList(decoderContext)
            // Special UUID handling? sure
            BsonType.BINARY -> {
                if (BsonBinarySubType.isUuid(peekBinarySubType()) && peekBinarySize() == 16) {
                    return codecRegistry[UUID::class.java].decode(this, decoderContext)
                }
            }
            else -> {
                // fall through to catch BINARY case above
            }
        }
        return bsonTypeCodecMap[currentBsonType].decode(this, decoderContext)
    }

    private fun BsonReader.readList(decoderContext: DecoderContext): Any? {
        readStartArray()
        val list = mutableListOf<Any?>()
        while (readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(readUnknownValue(decoderContext))
        }
        readEndArray()
        return list
    }

    private fun BsonReader.readMapLike(decoderContext: DecoderContext,
                                       entryConsumer: (String, Any?) -> Unit) {
        readStartDocument()
        while (readBsonType() != BsonType.END_OF_DOCUMENT) {
            val key = readName()
            val value = readValue(key, decoderContext)
            entryConsumer(key, value)
        }
        readEndDocument()
    }

}

class SimpleMessageCodec(
        codecRegistry: CodecRegistry,
        bsonTypeClassMap: BsonTypeClassMap,
        encoderClass: Class<Message>,
        defaultMessage: Message
) : MessageCodec(codecRegistry, bsonTypeClassMap, encoderClass, defaultMessage)

class CollectibleMessageCodec(
        codecRegistry: CodecRegistry,
        bsonTypeClassMap: BsonTypeClassMap,
        encoderClass: Class<Message>,
        defaultMessage: Message,
        private val idFieldDescriptor: Descriptors.FieldDescriptor
) : MessageCodec(codecRegistry, bsonTypeClassMap, encoderClass, defaultMessage), CollectibleCodec<Message> {

    override val Descriptors.FieldDescriptor.mongoDbName: String
        get() = when (this) {
            idFieldDescriptor -> "_id"
            else -> name
        }

    override fun generateIdIfAbsentFromDocument(document: Message): Message {
        if (documentHasId(document)) {
            return document
        }

        return document.newBuilderForType()
                .setField(idFieldDescriptor, UUID.randomUUID().toProtobuf())
                .build()
    }

    override fun documentHasId(document: Message): Boolean {
        return document.hasField(idFieldDescriptor)
    }

    override fun getDocumentId(document: Message): BsonValue {
        val protoUuid = document.getField(idFieldDescriptor) as ProtoUuid

        // hard-code the representation:
        return BsonDocument()
                .append("lo", BsonInt64(protoUuid.lo))
                .append("hi", BsonInt64(protoUuid.hi))
    }
}

