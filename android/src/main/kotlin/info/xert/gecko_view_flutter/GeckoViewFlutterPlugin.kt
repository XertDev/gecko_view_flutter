package info.xert.gecko_view_flutter

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

/** GeckoViewFlutterPlugin */
class GeckoViewFlutterPlugin: FlutterPlugin, ActivityAware {

    private lateinit var pluginBinding: FlutterPluginBinding

    private lateinit var context: Context

    private lateinit var runtimeController: GeckoRuntimeController
    private var proxy: GeckoProxy? = null

    override fun onAttachedToEngine(binding: FlutterPluginBinding) {
        pluginBinding = binding

        context = binding.applicationContext
        runtimeController = GeckoRuntimeController(context, binding.flutterAssets)
        proxy = GeckoProxy(binding.binaryMessenger, runtimeController)
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        if(proxy != null) {
            proxy!!.dispose()
            proxy = null
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        pluginBinding.platformViewRegistry.registerViewFactory(
                "gecko_view", GeckoViewFactory(pluginBinding.binaryMessenger, runtimeController)
        )
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        if(proxy != null) {
            proxy!!.dispose()
            proxy = null
        }
    }

}
