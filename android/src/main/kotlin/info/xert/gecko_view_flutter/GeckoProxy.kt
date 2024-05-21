package info.xert.gecko_view_flutter

import android.util.Log
import info.xert.gecko_view_flutter.common.InvalidArgumentException
import info.xert.gecko_view_flutter.common.NoArgumentException
import info.xert.gecko_view_flutter.common.ResultConsumer
import info.xert.gecko_view_flutter.common.unitResultConsumer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class GeckoProxy(
        messenger: BinaryMessenger,
        private val runtimeController: GeckoRuntimeController)
    : MethodChannel.MethodCallHandler {

    private val TAG: String = GeckoProxy::class.java.name

    private val channel: MethodChannel = MethodChannel(messenger, "gecko_view_flutter")

    init {
        Log.d(TAG, "Initializing GeckoPlugin")
    }

    override fun onMethodCall(call: MethodCall, callResult: MethodChannel.Result) {
        try {
            when (call.method) {
                "getPlatformVersion" -> {
                    callResult.success("Android ${android.os.Build.VERSION.RELEASE}")
                }

                "enableHostJSExecution" -> {
                    runtimeController.enableHostJsExecution(unitResultConsumer(callResult))
                }

                else -> {
                    callResult.notImplemented()
                }
            }
        } catch (e: InvalidArgumentException) {
            callResult.error("Invalid argument error", e.message, null)
        } catch (e: NoArgumentException) {
            callResult.error("No argument error", e.message, null)
        }
    }

    init {
        Log.d(TAG, "Initializing channels")

        channel.setMethodCallHandler(this)
    }

    fun dispose() {
        channel.setMethodCallHandler(null)
    }
}