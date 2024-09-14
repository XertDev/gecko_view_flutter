import 'package:gecko_view_flutter/src/common/cookie.dart';
import 'package:gecko_view_flutter/src/host/method_channel/method_channel_proxy.dart';

export 'src/gecko_view_controller.dart';
export 'package:gecko_view_flutter/src/common/find_request.dart';
export 'src/common/error.dart';

class GeckoCookieManager {
  GeckoCookieManager._();

  Future<Cookie> getCookie({
    String? firstPartyDomain,
    required String name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    required String url
  }) async {
    return await MethodChannelProxy.instance.getCookie(
        firstPartyDomain,
        name,
        partitionKey,
        storeId,
        url
    );
  }

  Future<List<Cookie>> getAllCookies({
    String? domain,
    String? firstPartyDomain,
    String? name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    required String url
  }) async {
    return await MethodChannelProxy.instance.getAllCookies(
        domain,
        firstPartyDomain,
        name,
        partitionKey,
        storeId,
        url
    );
  }

  Future<void> setCookie({
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
    required String url,
    String? value
  }) async {
    await MethodChannelProxy.instance.setCookie(
        domain,
        expirationDate,
        firstPartyDomain,
        httpOnly,
        name,
        partitionKey,
        path,
        sameSite,
        secure,
        storeId,
        url,
        value
    );
  }

  Future<void> removeCookie({
    String? firstPartyDomain,
    required String name,
    CookiePartitionKey? partitionKey,
    String? storeId,
    required String url
  }) async {
    await MethodChannelProxy.instance.removeCookie(
        firstPartyDomain,
        name,
        partitionKey,
        storeId,
        url
    );
  }
}

class GeckoViewFlutter {
  GeckoViewFlutter._();

  static Future<String> get platformVersion async {
    final String version = await MethodChannelProxy.instance.getPlatformVersion();
    return version;
  }
  
  static Future<void> enableHostJSExecution() async {
    await MethodChannelProxy.instance.enableHostJSExecution();
  }

  static Future<void> enableCookieManager() async {
    await MethodChannelProxy.instance.enableCookieManager();
  }

  static GeckoCookieManager cookieManager() {
    return GeckoCookieManager._();
  }
}