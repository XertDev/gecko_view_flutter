package info.xert.gecko_view_flutter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View

import info.xert.gecko_view_flutter.common.InvalidArgumentException
import info.xert.gecko_view_flutter.common.NoArgumentException
import info.xert.gecko_view_flutter.common.Offset
import info.xert.gecko_view_flutter.common.FindRequest
import info.xert.gecko_view_flutter.common.FindResult
import info.xert.gecko_view_flutter.common.Position
import info.xert.gecko_view_flutter.common.ResultConsumer
import info.xert.gecko_view_flutter.common.tryExtractSingleArgument
import info.xert.gecko_view_flutter.common.tryExtractStructure
import info.xert.gecko_view_flutter.handler.AlertPromptRequest
import info.xert.gecko_view_flutter.handler.ChoicePromptRequest
import info.xert.gecko_view_flutter.handler.PromptHandler
import info.xert.gecko_view_flutter.handler.PromptRequest

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView



class GeckoViewProxy(
        context: Context,
        runtimeManager: GeckoRuntimeController,
        messenger: BinaryMessenger,
        id: Int)
    : PlatformView, MethodChannel.MethodCallHandler, PromptHandler {



    private val TAG: String = GeckoViewInstance::class.java.name

    private val channel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_$id")
    private val tabChannel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_${id}_tab")
    private val promptChannel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter_${id}_prompt")

    private val instance: GeckoViewInstance

    init {
        Log.d(TAG, "Initializing GeckoView")
        instance = GeckoViewInstance(context, runtimeManager, this)
    }

    private fun invokeMethodUIThread(channel: MethodChannel, method: String, data: Map<String, Any?>, callback: MethodChannel.Result) {
        Handler(Looper.getMainLooper()).post {
            channel.invokeMethod(method, data, callback)
        }
    }

    private val onTabMethodCall =
        object : MethodChannel.MethodCallHandler {
            override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
                try {
                    val tabId = tryExtractSingleArgument<Int>(call, "tabId")

                    when (call.method) {
                        "isActive" -> {
                            result.success(instance.isTabActive(tabId))
                        }

                        "activate" -> {
                            instance.activateTab(tabId)
                            result.success(true)
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
                            val uri = tryExtractSingleArgument<String>(call, "uri")
                            instance.openURI(tabId, uri)
                            result.success(true)
                        }

                        "reload" -> {
                            instance.reload(tabId)
                            result.success(true)
                        }

                        "goBack" -> {
                            instance.goBack(tabId)
                            result.success(true)
                        }

                        "goForward" -> {
                            instance.goForward(tabId)
                            result.success(true)
                        }

                        "getScrollOffset" -> {
                            result.success(instance.getScrollOffset(tabId).toMap())
                        }

                        "scrollToBottom" -> {
                            instance.scrollToBottom(tabId)
                            result.success(true)
                        }

                        "scrollToTop" -> {
                            instance.scrollToTop(tabId)
                            result.success(true)
                        }

                        "scrollBy" -> {
                            val offset = tryExtractStructure<Offset>(call, "offset", Offset)
                            val smooth = tryExtractSingleArgument<Boolean>(call, "smooth")
                            instance.scrollBy(tabId, offset, smooth)
                            result.success(true)
                        }

                        "scrollTo" -> {
                            val position = tryExtractStructure(call, "position", Position)
                            val smooth = tryExtractSingleArgument<Boolean>(call, "smooth")
                            instance.scrollTo(tabId, position, smooth)
                            result.success(true)
                        }

                        "runJSAsync" -> {
                            val script = tryExtractSingleArgument<String>(call, "script")
                            instance.runJsAsync(tabId, script)
                            result.success(true)
                        }

                        "findNext" -> {
                            val request = tryExtractStructure(call, "request", FindRequest)
                            instance.findNext(
                                tabId, request,
                                object: ResultConsumer<FindResult> {
                                    override fun success(findResult: FindResult) {
                                        result.success(findResult.toMap())
                                    }

                                    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                        result.error(errorCode, errorMessage, errorDetails)
                                    }
                                }
                            )
                        }

                        "findClear" -> {
                            instance.findClear(tabId)
                            result.success(true)
                        }

                        else -> {
                            result.notImplemented()
                        }
                    }
                } catch (e: InvalidArgumentException) {
                    result.error("Invalid argument error", e.message, null)
                } catch (e: NoArgumentException) {
                    result.error("No argument error", e.message, null)
                }
            }
        }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when (call.method) {
                "init" -> {
                    instance.init()
                    result.success(true)
                }

                "createTab" -> {
                    val tabId = tryExtractSingleArgument<Int>(call, "tabId")
                    instance.createTab(tabId)
                    result.success(true)
                }

                "getActiveTab" -> {
                    val currentTab = instance.getActiveTabId()
                    result.success(currentTab)
                }

                else -> {
                    result.notImplemented()
                }
            }
        } catch (e: InvalidArgumentException) {
            result.error("Invalid argument error", e.message, null)
        } catch (e: NoArgumentException) {
            result.error("No argument error", e.message, null)
        }
    }

    init {
        Log.d(TAG, "Initializing channels")

        channel.setMethodCallHandler(this)
        tabChannel.setMethodCallHandler(onTabMethodCall)
    }

    override fun getView(): View {
        return instance.view
    }

    override fun dispose() {
    }

    private fun handlePrompt(request: PromptRequest, callback: ResultConsumer<Any?>, type: String) {
        invokeMethodUIThread(promptChannel, type, request.toMap(), object: MethodChannel.Result {
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

    override fun onChoicePrompt(request: ChoicePromptRequest, callback: ResultConsumer<Any?>) {
        handlePrompt(request, callback, "choicePrompt")
    }

    override fun onAlertPrompt(request: AlertPromptRequest, callback: ResultConsumer<Any?>) {
        handlePrompt(request, callback, "alertPrompt")
    }
}