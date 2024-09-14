package info.xert.gecko_view_flutter.common

data class Position(val x: Int, val y: Int): OutputStructure {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["x"] = x
        map["y"] = y
        return map
    }

    companion object: InputStructure<Position> {
        override fun fromMap(inputMap: Map<*, *>): Position {
            return Position(
                    x = inputMap["x"] as Int,
                    y = inputMap["y"] as Int
            )
        }
    }
}

data class Offset(val x: Int, val y: Int): OutputStructure {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["x"] = x
        map["y"] = y
        return map
    }

    companion object: InputStructure<Offset> {
        override fun fromMap(inputMap: Map<*, *>): Offset {
            return Offset(
                    x = inputMap["x"] as Int,
                    y = inputMap["y"] as Int
            )
        }
    }
}