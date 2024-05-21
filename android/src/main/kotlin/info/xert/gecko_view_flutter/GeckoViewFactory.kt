package info.xert.gecko_view_flutter

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterAssets
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class GeckoViewFactory(private val messenger: BinaryMessenger,
                       private val runtimeController: GeckoRuntimeController)
    : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        return GeckoViewProxy(context!!, runtimeController, messenger, viewId)
    }
}