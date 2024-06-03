package info.xert.gecko_view_flutter.common

import org.json.JSONObject

fun toValueOrNull(json: JSONObject, key: String): Any? {
    if (!json.has(key)) {
        throw RuntimeException("Invalid map key")
    }

    if (!json.isNull(key)) {
        return json.get(key)
    }

    return null
}
interface JSONOutputStructure {
    fun toJSON(): JSONObject
}
interface OutputStructure {
    fun toMap(): Map<String, Any?>
}

interface JSONInputStructure<T> {
    fun fromJSON(inputJSON: JSONObject) : T
}
interface InputStructure<T> {
    fun fromMap(inputMap: Map<*, *>): T
}