package info.xert.gecko_view_flutter.common

enum class FindDirection(val value: String) {
    FORWARD("forward"),
    BACKWARDS("backwards");
}

data class FindRequest(
    val searchString: String,
    val direction: FindDirection,
    val matchCase: Boolean,
    val wholeWord: Boolean,
    val linksOnly: Boolean,
    val highlightAll: Boolean,
    val dimPage: Boolean,
    val drawLinkOutline: Boolean
) {
    companion object: InputStructure<FindRequest> {
        override fun fromMap(inputMap: Map<*, *>): FindRequest {
            return FindRequest(
                searchString = inputMap["searchString"] as String,
                direction = FindDirection.entries.first { dir -> dir.value == inputMap["direction"] as String },
                matchCase = inputMap["matchCase"] as Boolean,
                wholeWord = inputMap["wholeWord"] as Boolean,
                linksOnly = inputMap["linksOnly"] as Boolean,
                highlightAll = inputMap["highlightAll"] as Boolean,
                dimPage = inputMap["dimPage"] as Boolean,
                drawLinkOutline = inputMap["drawLinkOutline"] as Boolean
            )
        }
    }
}

data class FindResult(
    val matched: Boolean,
    val occurrenceOffset: Int,
    val linkUri: String,
    val total: Int,
    val wrapped: Boolean
): OutputStructure {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["matched"] = matched
        map["occurrenceOffset"] = occurrenceOffset
        map["linkUri"] = linkUri
        map["total"] = total
        map["wrapped"] = wrapped

        return map
    }
}