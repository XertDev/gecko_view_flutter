import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:gecko_view_flutter/src/common/error.dart';
import 'package:gecko_view_flutter/src/common/find_request.dart';
import 'package:gecko_view_flutter/src/common/find_response.dart';
import 'package:gecko_view_flutter/src/common/position.dart';
import 'package:gecko_view_flutter/src/host/method_channel/method_channel_prompt_handler.dart';
import 'package:gecko_view_flutter/src/common/cookie.dart';


class MethodChannelProxy {
  static final MethodChannelProxy _instance = MethodChannelProxy();

  static MethodChannelProxy get instance {
    return _instance;
  }

  static String channelPrefix = "gecko_view_flutter";

  static MethodChannel openChannel() {
    return MethodChannel(channelPrefix);
  }

  static MethodChannel openViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_$viewId');
  }

  static MethodChannel openTabViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_${viewId}_tab');
  }

  static MethodChannel openPromptViewChannel(int viewId) {
    return MethodChannel('${channelPrefix}_${viewId}_prompt');
  }

  static const COOKIE_MANAGER_KEY = "Cookie Manager";

  static void _handleCookieManagerException(PlatformException exception) {
    if (exception.code == COOKIE_MANAGER_KEY) {
      throw CookieException(exception.message ?? "");
    }
  }

  MethodChannelPromptHandler registerPromptHandler(int viewId) {
    var channel = openPromptViewChannel(viewId);
    return MethodChannelPromptHandler(channel);
  }

  static Future<T?> invokeMethodForPlugin<T>(String command, Map<String, Object?> args) async {
    try {
      var channel = openChannel();
      debugPrint("Calling $command for plugin");

      return await channel.invokeMethod<T>(command, args);
    } on PlatformException catch (e) {
      debugPrint("${e.code}: ${e.message}");
      rethrow;
    }
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

  Future<int?> getActiveTab(int viewId) async {
    try {
      var channel = openViewChannel(viewId);
      return await channel.invokeMethod<int>("getActiveTab", {});
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

  Future<GeckoOffset> getScrollOffset(int viewId, int tabId) async {
    final result = await invokeMethodForTab<Map<Object?, Object?>>(viewId, tabId, "getScrollOffset", {});
    return GeckoOffset.fromMap(result!);
  }

  Future<void> scrollToBottom(int viewId, int tabId) async {
    await invokeMethodForTab(viewId, tabId, "scrollToBottom", {});
  }

  Future<void> scrollToTop(int viewId, int tabId) async {
    await invokeMethodForTab(viewId, tabId, "scrollToTop", {});
  }

  Future<void> scrollBy(int viewId, int tabId, GeckoOffset offset, bool smooth) async {
    await invokeMethodForTab(viewId, tabId, "scrollBy", {
      "smooth": smooth,
      "offset": offset.toMap()
    });
  }

  Future<void> scrollTo(int viewId, int tabId, GeckoPosition position, bool smooth) async {
    await invokeMethodForTab(viewId, tabId, "scrollTo", {
      "smooth": smooth,
      "position": position.toMap()
    });
  }

  Future<GeckoFindResult> findNext(int viewId, int tabId, GeckoFindRequest request) async {
    final result = await invokeMethodForTab<Map<Object?, Object?>>(
      viewId, tabId,
      "findNext", {
        "request": request.toMap()
      }
    );
    return GeckoFindResult.fromMap(result!);
  }

  Future<void> findClear(int viewId, int tabId) async {
    await invokeMethodForTab(viewId, tabId, "findClear", {});
  }

  Future<void> enableHostJSExecution() async {
    await invokeMethodForPlugin("enableHostJSExecution", {});
  }

  Future<void> runJSAsync(int viewId, int tabId, String script) async {
    await invokeMethodForTab(viewId, tabId, "runJSAsync", {
      "script": script
    });
  }

  Future<void> enableCookieManager() async {
    await invokeMethodForPlugin("enableCookieManager", {});
  }

  Future<Cookie> getCookie(
    String? firstPartyDomain,
    String name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    String url)
  async {
    try {
      final result = await invokeMethodForPlugin<Map<Object?, Object?>>(
          "getCookie", {
        "firstPartyDomain": firstPartyDomain,
        "name": name,
        "partitionKey": partitionKey?.toMap(),
        "storeId": storeId,
        "url": url
      });
      return Cookie.fromMap(result!);
    } on PlatformException catch (e) {
      _handleCookieManagerException(e);
      rethrow;
    }
  }

  Future<List<Cookie>> getAllCookies(
    String? domain,
    String? firstPartyDomain,
    String? name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    String url)
  async {
    try {
      final result = await invokeMethodForPlugin<List<Object?>>(
          "getAllCookies", {
        "domain": domain,
        "firstPartyDomain": firstPartyDomain,
        "name": name,
        "partitionKey": partitionKey?.toMap(),
        "storeId": storeId,
        "url": url
      });

      return result!.map((entry) =>
          Cookie.fromMap(entry as Map<Object?, Object?>)).toList();
    } on PlatformException catch (e) {
      _handleCookieManagerException(e);
      rethrow;
    }
  }

  Future<void> setCookie(
    String? domain,
    int? expirationDate,
    String? firstPartyDomain,
    bool? httpOnly,
    String? name,
    CookiePartitionKey? partitionKey,
    String? path,
    CookieSameSiteStatus? sameSite,
    bool? secure,
    String? storeId,
    String url,
    String? value)
  async {
    try {
      await invokeMethodForPlugin(
          "setCookie", {
        "domain": domain,
        "expirationDate": expirationDate,
        "firstPartyDomain": firstPartyDomain,
        "httpOnly": httpOnly,
        "name": name,
        "partitionKey": partitionKey?.toMap(),
        "path": path,
        "sameSite": sameSite?.value,
        "secure": secure,
        "storeId": storeId,
        "url": url,
        "value": value
      });
    } on PlatformException catch (e) {
      _handleCookieManagerException(e);
      rethrow;
    }
  }

  Future<void> removeCookie(
    String? firstPartyDomain,
    String name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    String url)
  async {
    try {
      await invokeMethodForPlugin(
          "removeCookie", {
        "firstPartyDomain": firstPartyDomain,
        "name": name,
        "partitionKey": partitionKey?.toMap(),
        "storeId": storeId,
        "url": url
      });
    } on PlatformException catch (e) {
      _handleCookieManagerException(e);
      rethrow;
    }
  }

  Future<String> getPlatformVersion() async {
    return (await invokeMethodForPlugin("getPlatformVersion", {}))!;
  }
}