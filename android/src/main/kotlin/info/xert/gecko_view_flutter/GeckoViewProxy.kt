package info.xert.gecko_view_flutter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import info.xert.gecko_view_flutter.handler.ChoicePromptRequest
import info.xert.gecko_view_flutter.handler.PromptHandler
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class GeckoViewProxy(
        context: Context,
        messenger: BinaryMessenger,
        id: Int)
    : PlatformView, MethodChannel.MethodCallHandler, PromptHandler {

    interface Result {
        fun success(result: Any?)
        fun error(errorCode: String, errorMessage: String?, errorDetails: Any?)
    }

    private val TAG: String = GeckoViewInstance::class.java.name

    private val channel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_$id")
    private val tabChannel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_${id}_tab")
    private val promptChannel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_${id}_prompt")

    private val instance: GeckoViewInstance

    init {
        Log.d(TAG, "Initializing GeckoView")
        instance = GeckoViewInstance(context, this)
    }

    private fun invokeMethodUIThread(channel: MethodChannel, method: String, data: Map<String, Any?>, callback: MethodChannel.Result) {
        Handler(Looper.getMainLooper()).post {
            channel.invokeMethod(method, data, callback)
        }
    }

    private val onTabMethodCall =
        object : MethodChannel.MethodCallHandler {
            override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
                val tabId = call.argument<Int>("tabId")
                if (tabId == null) {
                    result.error("Argument error", "No tab id provided", null)
                    return
                }

                when (call.method) {
                    "isActive" -> {
                        result.success(instance.isTabActive(tabId))
                    }

                    "activate" -> {
                        instance.activateTab(tabId)
                    }

                    "getCurrentUrl" -> {
                        result.success(instance.currentUrl(tabId))
                    }

                    "getTitle" -> {
                        result.success(instance.currentUrl(tabId))
                    }

                    "getUserAgent" -> {
                        result.success(instance.getUserAgent(tabId))
                    }

                    "openURI" -> {
                        val uri = call.argument<String>("uri")
                        if (uri == null) {
                            result.error("Argument error", "No uri provided", null)
                            return
                        } else {
                            instance.openURI(tabId, uri)
                        }
                    }

                    "reload" -> {
                        instance.reload(tabId)
                    }

                    "goBack" -> {
                        instance.goBack(tabId)
                    }

                    "goForward" -> {
                        instance.goForward(tabId)
                    }

                    else -> {
                        result.notImplemented()
                    }
                }
            }
        }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "init" -> {
                instance.init()
                result.success(true)
            }

            "createTab" -> {
                val tabId = call.argument<Int>("tabId")
                if(tabId != null) {
                    instance.createTab(tabId)
                    result.success(true)
                } else {
                    result.error("Argument error", "No tab id provided", null)
                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    init {
        Log.d(TAG, "Initializing channels")

        channel.setMethodCallHandler(this)
        tabChannel.setMethodCallHandler(onTabMethodCall)
    }

    override fun getView(): View {
        return instance.getView()
    }

    override fun dispose() {
    }

    override fun onChoicePrompt(request: ChoicePromptRequest, callback: GeckoViewProxy.Result) {
        invokeMethodUIThread(promptChannel, "choicePrompt", request.toMap(), object: MethodChannel.Result {
            override fun success(result: Any?) {
                callback.success(result)
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                callback.error(errorCode, errorMessage, errorDetails)
            }

            override fun notImplemented() {
                callback.error("Internal", "Not implemented", null)
            }
        })
    }
}