package info.xert.gecko_view_flutter.common

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class NoArgumentException(message: String? = null, cause: Throwable? = null)
    : Exception(message, cause)

class InvalidArgumentException(message: String? = null, cause: Throwable? = null)
    : Exception(message, cause)
fun <T> tryExtractSingleArgument(call: MethodCall, nodeName: String): T {
    if (!call.hasArgument(nodeName)) {
        throw NoArgumentException("No $nodeName provided")
    } else {
        try {
            return call.argument<T>(nodeName)!!
        } catch (e: ClassCastException) {
            throw InvalidArgumentException("Invalid type for $nodeName provided")
        }
    }
}

fun <T> tryExtractStructure(call: MethodCall, nodeName: String, companion: InputStructure<T>): T {
    if (!call.hasArgument(nodeName)) {
        throw NoArgumentException("No $nodeName provided")
    } else {
        try {
            val structureMap = call.argument<Map<*, *>>(nodeName)!!
            return companion.fromMap(structureMap)
        } catch (e: ClassCastException) {
            throw InvalidArgumentException("Invalid type for $nodeName provided")
        }
    }
}

fun unitResultConsumer(callResult: MethodChannel.Result): ResultConsumer<Unit> {
    return object: ResultConsumer<Unit> {
        override fun success(result: Unit) {
            callResult.success(true);
        }

        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
            callResult.error(errorCode, errorMessage, errorDetails)
        }

    }
}