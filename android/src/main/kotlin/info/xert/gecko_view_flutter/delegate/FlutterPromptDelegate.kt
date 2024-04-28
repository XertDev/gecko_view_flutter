package info.xert.gecko_view_flutter.delegate

import info.xert.gecko_view_flutter.GeckoViewProxy
import info.xert.gecko_view_flutter.handler.ChoicePromptRequest
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PromptDelegate.PromptResponse

class FlutterPromptDelegate(private val proxy: GeckoViewProxy): GeckoSession.PromptDelegate {
    private fun geckoChoiceToChoice(choice: GeckoSession.PromptDelegate.ChoicePrompt.Choice): ChoicePromptRequest.Choice {
        return ChoicePromptRequest.Choice(
                choice.id,
                choice.disabled,
                choice.icon,
                choice.items?.map { innerChoice -> geckoChoiceToChoice(innerChoice) },
                choice.label,
                choice.selected,
                choice.separator
        )
    }

    private fun geckoChoicePromptToRequest(prompt: GeckoSession.PromptDelegate.ChoicePrompt): ChoicePromptRequest {
        return ChoicePromptRequest(
                prompt.title,
                prompt.message,
                prompt.type,
                prompt.choices.map { choice -> geckoChoiceToChoice(choice) }
        )
    }

    override fun onChoicePrompt(
            session: GeckoSession,
            prompt: GeckoSession.PromptDelegate.ChoicePrompt): GeckoResult<GeckoSession.PromptDelegate.PromptResponse>? {
        val response: GeckoResult<PromptResponse> = GeckoResult<PromptResponse>()

        val request: ChoicePromptRequest = geckoChoicePromptToRequest(prompt)
        proxy.onChoicePrompt(request, object: GeckoViewProxy.Result {
            override fun success(result: Any?) {
                val responseMap = result as Map<*, *>
                if(responseMap["confirmed"] as Boolean) {
                    val ids = responseMap["responses"] as List<*>
                    val responses = ids.map { id -> id as String }.toTypedArray()

                    response.complete(prompt.confirm(responses))
                } else {
                    response.complete(prompt.dismiss())
                }
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                response.complete(prompt.dismiss())
            }
        })

        return response
    }
}