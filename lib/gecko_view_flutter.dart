import 'package:flutter/services.dart';

export 'src/gecko_view_controller.dart';

class GeckoViewFlutter {
  static const MethodChannel _channel =
      MethodChannel("gecko_view_flutter");

  GeckoViewFlutter._();

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod("getPlatformVersion");
    return version;
  }
}