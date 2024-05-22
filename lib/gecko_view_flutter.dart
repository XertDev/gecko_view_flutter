import 'package:flutter/services.dart';
import 'package:gecko_view_flutter/src/host/method_channel/method_channel_proxy.dart';

export 'src/gecko_view_controller.dart';

class GeckoViewFlutter {
  GeckoViewFlutter._();

  static Future<String> get platformVersion async {
    final String version = await MethodChannelProxy.instance.getPlatformVersion();
    return version;
  }
  
  static Future<void> enableHostJSExecution() async {
    await MethodChannelProxy.instance.enableHostJSExecution();
  }
}