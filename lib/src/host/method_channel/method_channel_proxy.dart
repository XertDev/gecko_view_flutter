import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:gecko_view_flutter/src/host/method_channel/method_channel_prompt_handler.dart';


class MethodChannelProxy {
  static final MethodChannelProxy _instance = MethodChannelProxy();

  static MethodChannelProxy get instance {
    return _instance;
  }

  static String channelPrefix = "gecko_view_flutter";

  static MethodChannel openViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_$viewId');
  }

  static MethodChannel openTabViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_${viewId}_tab');
  }

  static MethodChannel openPromptViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_${viewId}_prompt');
  }

  MethodChannelPromptHandler registerPromptHandler(int viewId) {
    var channel = openPromptViewChannel(viewId);
    return MethodChannelPromptHandler(channel);
  }

  static Future<T?> invokeMethodForTab<T>(int viewId, int tabId, String command, Map<String, Object?> args) async {
    try {
      var channel = openTabViewChannel(viewId);
      debugPrint("Calling $command for view: $viewId tab: $tabId");

      final Map<String, Object?> fullArgs = {
        "tabId": tabId
      };
      fullArgs.addAll(args);

      return await channel.invokeMethod<T>(command, fullArgs);
    } on PlatformException catch (e) {
      debugPrint("${e.code}: ${e.message}");
      rethrow;
    }
  }
  
  Future<void> register(int viewId) async {
    try {
      var channel = openViewChannel(viewId);
      await channel.invokeMethod<void>("init");
    } on PlatformException catch (e) {
      debugPrint("${e.code}: ${e.message}");
      rethrow;
    }
  }

  Future<void> createTab(int viewId, int tabId) async {
    try {
      var channel = openViewChannel(viewId);
      await channel.invokeMethod<void>("createTab", {
        "tabId": tabId
      });
    } on PlatformException catch (e) {
      debugPrint("${e.code}: ${e.message}");
      rethrow;
    }
  }

  Future<bool> isTabActive(int viewId, int tabId) async {
    return (await invokeMethodForTab<bool>(viewId, tabId, "isActive", {})) ?? false;
  }

  Future<void> activateTab(int viewId, int tabId) async {
    await invokeMethodForTab<void>(viewId, tabId, "activate", {});
  }

  Future<String?> getCurrentUrl(int viewId, int tabId) async {
    return await invokeMethodForTab<String?>(viewId, tabId, "getCurrentUrl", {});
  }

  Future<String?> getTitle(int viewId, int tabId) async {
    return await invokeMethodForTab<String?>(viewId, tabId, "getTitle", {});
  }

  Future<String?> getUserAgent(int viewId, int tabId) async {
    return await invokeMethodForTab<String?>(viewId, tabId, "getUserAgent", {});
  }

  Future<void> openURI(int viewId, int tabId, Uri uri) async {
    await invokeMethodForTab<void>(viewId, tabId, "openURI", {
      "uri": uri.toString()
    });
  }

  Future<void> reload(int viewId, int tabId) async {
    await invokeMethodForTab<void>(viewId, tabId, "reload", {});
  }

  Future<void> goBack(int viewId, int tabId) async {
    await invokeMethodForTab<void>(viewId, tabId, "goBack", {});
  }

  Future<void> goForward(int viewId, int tabId) async {
    await invokeMethodForTab<void>(viewId, tabId, "goForward", {});
  }
}