package info.xert.gecko_view_flutter.common

interface OutputStructure {
    fun toMap(): Map<String, Any?>;
}

interface InputStructure<T> {
    fun fromMap(inputMap: Map<*, *>): T;
}