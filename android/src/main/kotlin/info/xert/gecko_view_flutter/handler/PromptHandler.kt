package info.xert.gecko_view_flutter.handler

import info.xert.gecko_view_flutter.common.ResultConsumer

interface PromptRequest {
    fun toMap(): Map<String, Any?>
}

data class ChoicePromptRequest(
        val title: String?,
        val message: String?,
        val type: Int,
        val choices: List<Choice>
): PromptRequest {
    data class Choice(
            val id: String,
            val disabled: Boolean,
            val icon: String?,
            val items: List<Choice>?,
            val label: String,
            val selected: Boolean,
            val separator: Boolean
    ) {
        fun toMap(): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            map["id"] = id
            map["disabled"] = disabled
            map["icon"] = icon
            map["items"] = items?.map { choice -> choice.toMap() }
            map["label"] = label
            map["selected"] = selected
            map["separator"] = separator
            return map
        }
    }

    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["title"] = title
        map["message"] = message
        map["type"] = type
        map["choices"] = choices.map { choice -> choice.toMap() }
        return map
    }
}

data class AlertPromptRequest(
        val title: String?,
        val message: String?
): PromptRequest {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["title"] = title
        map["message"] = message
        return map
    }
}
interface PromptHandler {
    fun onChoicePrompt(request: ChoicePromptRequest, callback: ResultConsumer<Any?>)
    fun onAlertPrompt(request: AlertPromptRequest, callback: ResultConsumer<Any?>)
}