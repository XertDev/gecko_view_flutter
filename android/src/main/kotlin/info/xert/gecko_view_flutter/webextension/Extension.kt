package info.xert.gecko_view_flutter.webextension

import android.os.Handler
import android.os.Looper
import android.util.Log
import info.xert.gecko_view_flutter.common.ResultConsumer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtension.PortDelegate

abstract class Extension {
    abstract val TAG: String

    abstract val extensionID: String
    abstract val extensionPath: String

    var enabled: Boolean = false
        private set

    var messageHandler: (Any) -> Unit = {
        _: Any -> Unit
    }

    protected val portDelegate: WebExtension.PortDelegate = object: PortDelegate {
        override fun onPortMessage(message: Any, port: WebExtension.Port) {
            messageHandler(message)
        }
    }

    protected val messageDelegate: WebExtension.MessageDelegate = object: WebExtension.MessageDelegate {
        override fun onConnect(newPort: WebExtension.Port) {
            newPort.setDelegate(portDelegate)
            port = newPort
        }
    }

    protected var port: WebExtension.Port? = null;
    var extension: WebExtension? = null
        private set
    fun enable(runtime: GeckoRuntime, assets: FlutterPlugin.FlutterAssets, callback: ResultConsumer<Unit>) {
        Log.d(TAG, "Initializing $extensionID Extension")
        if (extension == null) {
            val extensionPath = assets.getAssetFilePathBySubpath(extensionPath, "gecko_view_flutter")
                    ?: throw InternalError("Invalid plugin installation")

            runtime.webExtensionController.ensureBuiltIn("resource://android/assets/$extensionPath", extensionID)
                    .accept(
                            { newExtension ->
                                Handler(Looper.getMainLooper()).post {
                                    if (newExtension != null) {
                                        extension = newExtension
                                        newExtension.setMessageDelegate(
                                                messageDelegate,
                                                "browser"
                                        )

                                        enabled = true
                                        Log.d(TAG, "$extensionID Extension initialized")
                                    }

                                    callback.success(Unit)
                                }
                            },
                            { e ->
                                Log.e(TAG, "Error registering $extensionID Extension", e)
                                callback.error(TAG, "Error registering $extensionID Extension", e)
                            }
                    )
        }
    }
}