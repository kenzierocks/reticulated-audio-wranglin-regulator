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
import mu.KotlinLogging
import net.octyl.rawr.gen.protos.RawrCall
import net.octyl.rawr.gen.protos.RawrException
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters

private val logger = KotlinLogging.logger { }

/**
 * Metadata for translating [RawrCall].
 */
private data class RawrCallMetadata(
    val function: KFunction<Any?>,
    val argumentTranslators: List<ArgumentTranslator>
)

private class ArgumentTranslator(
        private val messageInstance: MessageLite
) {
    fun translate(byteString: ByteString): MessageLite {
        return messageInstance.parserForType.parseFrom(byteString)
    }
}

object RawrCallTranslator {
    private val rawrServiceCalls: Map<String, RawrCallMetadata> = RawrService::class.memberFunctions
        .filter { func -> func.annotations.any { it is RawrCallMarker } }
        .associate { func ->
            val rawrCallCode = getRawrCallCode(func)
            logger.debug { "Associating RawrCall $func with $rawrCallCode" }
            rawrCallCode to rawrCallMetadata(func)
        }

    fun initialize() {
        // does nothing, implicitly loads `rawrServiceCalls`
    }

    /**
     * Translates a RawrCall into an actual call.
     */
    suspend fun RawrService.call(rawrCall: RawrCall): MessageLite? {
        val (code, args) = rawrCall.functionCode to rawrCall.argumentsList
        val handle = rawrServiceCalls[code]
            ?: throw IllegalStateException("Invalid RPC call `$code`")

        val argArray = Array(args.size + 1) { i ->
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (i) {
                0 -> this
                else -> handle.argumentTranslators[i - 1].translate(args[i - 1])
            }
        }
        return try {
            callFunction(handle, argArray)
        } catch (e: Exception) {
            val incidentId = UUID.randomUUID().toString()
            logger.warn(e) { "Exception occurred in RPC, incidentId=$incidentId" }
            RawrException.newBuilder()
                .setIncidentId(incidentId)
                .setMessage(e.message ?: e.javaClass.name)
                .build()
        }
    }

    private suspend fun callFunction(handle: RawrCallMetadata, argArray: Array<Any>): MessageLite? {
        return when (val message = handle.function.callSuspend(*argArray)) {
            is MessageLite -> message
            is RawrCursorPage<*> -> message.toProto()
            null -> null
            else -> {
                logger.warn { "Non-MessageLite response from ${handle.function}" }
                null
            }
        }
    }

    private fun isOkReturnType(returnType: KClass<*>): Boolean {
        return returnType == Unit::class || returnType.isSubclassOf(MessageLite::class) ||
            returnType.isSubclassOf(RawrCursorPage::class)
    }

    private fun rawrCallMetadata(func: KFunction<*>): RawrCallMetadata {
        val returnType = (func.returnType.classifier as KClass<*>)
        check(isOkReturnType(returnType)) {
            "RawrCallMarker function `$func` does not return MessageLite, RawrCursorPage or Unit"
        }
        val allParamsValid = func.valueParameters
            .map { it.type.classifier as? KClass<*> }
            .all { it != null && it.isSubclassOf(MessageLite::class) }
        check(allParamsValid) {
            "RawrCallMarker function `$func` does not take all MessageLite parameters"
        }
        // checked right above :)
        @Suppress("UNCHECKED_CAST")
        val castFunc = func as KFunction<Any?>
        return RawrCallMetadata(castFunc, func.valueParameters.map { param ->
            val messageType = (param.type.classifier as KClass<*>)
            val instance = messageType.declaredFunctions.single {
                it.parameters.isEmpty() && it.name == "getDefaultInstance"
            }.call() as MessageLite

            ArgumentTranslator(instance)
        })
    }
}
