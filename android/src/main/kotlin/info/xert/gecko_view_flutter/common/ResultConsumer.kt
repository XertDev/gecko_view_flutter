package info.xert.gecko_view_flutter.common

interface ResultConsumer<T> {
    fun success(result: T)
    fun error(errorCode: String, errorMessage: String?, errorDetails: Any?)
}