package info.xert.gecko_view_flutter

import android.util.Log
import info.xert.gecko_view_flutter.common.Cookie
import info.xert.gecko_view_flutter.common.CookiePartitionKey
import info.xert.gecko_view_flutter.common.CookieSameSiteStatus
import info.xert.gecko_view_flutter.common.InvalidArgumentException
import info.xert.gecko_view_flutter.common.NoArgumentException
import info.xert.gecko_view_flutter.common.ResultConsumer
import info.xert.gecko_view_flutter.common.tryExtractOptionalSingleArgument
import info.xert.gecko_view_flutter.common.tryExtractOptionalStructure
import info.xert.gecko_view_flutter.common.tryExtractSingleArgument
import info.xert.gecko_view_flutter.common.tryExtractStructure
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

                "enableCookieManager" -> {
                    runtimeController.enableCookieManager(unitResultConsumer(callResult))
                }

                "getCookie" -> {
                    if (!runtimeController.cookieManagerExtension.enabled) {
                        callResult.error(
                                "Invalid state",
                                "Cookie manager error not initialized",
                                "Cookie manager extension need to be enabled to manage cookies"
                        )
                        return
                    }

                    val firstPartyDomain = tryExtractOptionalSingleArgument<String>(call, "firstPartyDomain")
                    val name = tryExtractSingleArgument<String>(call, "name")
                    val partitionKey = tryExtractOptionalStructure(call, "partitionKey", CookiePartitionKey)
                    val storeId = tryExtractOptionalSingleArgument<String>(call, "storeId")
                    val url = tryExtractSingleArgument<String>(call, "url")

                    runtimeController.cookieManagerExtension.getCookie(
                            firstPartyDomain, name, partitionKey,
                            storeId, url,
                            object: ResultConsumer<Cookie> {
                                override fun success(result: Cookie) {
                                    callResult.success(result.toMap())
                                }

                                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                    callResult.error(errorCode, errorMessage, errorDetails)
                                }
                            }
                    )
                }

                "getAllCookies" -> {
                    if (!runtimeController.cookieManagerExtension.enabled) {
                        callResult.error(
                                "Invalid state",
                                "Cookie manager error not initialized",
                                "Cookie manager extension need to be enabled to manage cookies"
                        )
                        return
                    }

                    val domain = tryExtractOptionalSingleArgument<String>(call, "domain")
                    val firstPartyDomain = tryExtractOptionalSingleArgument<String>(call, "firstPartyDomain")
                    val name = tryExtractOptionalSingleArgument<String>(call, "name")
                    val partitionKey = tryExtractOptionalStructure(call, "partitionKey", CookiePartitionKey)
                    val storeId = tryExtractOptionalSingleArgument<String>(call, "storeId")
                    val url = tryExtractSingleArgument<String>(call, "url")

                    runtimeController.cookieManagerExtension.getAllCookie(
                            domain, firstPartyDomain, name, partitionKey,
                            storeId, url,
                            object: ResultConsumer<List<Cookie>> {
                                override fun success(result: List<Cookie>) {
                                    callResult.success(result.map { cookie -> cookie.toMap() }.toList())
                                }

                                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                    callResult.error(errorCode, errorMessage, errorDetails)
                                }
                            }
                    )
                }

                "removeCookie" -> {
                    if (!runtimeController.cookieManagerExtension.enabled) {
                        callResult.error(
                                "Invalid state",
                                "Cookie manager error not initialized",
                                "Cookie manager extension need to be enabled to manage cookies"
                        )
                        return
                    }

                    val firstPartyDomain = tryExtractOptionalSingleArgument<String>(call, "firstPartyDomain")
                    val name = tryExtractSingleArgument<String>(call, "name")
                    val partitionKey = tryExtractOptionalStructure(call, "partitionKey", CookiePartitionKey)
                    val storeId = tryExtractOptionalSingleArgument<String>(call, "storeId")
                    val url = tryExtractSingleArgument<String>(call, "url")

                    runtimeController.cookieManagerExtension.removeCookie(
                            firstPartyDomain, name, partitionKey, storeId, url,
                            object: ResultConsumer<Unit> {
                                override fun success(result: Unit) {
                                    callResult.success(true)
                                }

                                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                    callResult.error(errorCode, errorMessage, errorDetails)
                                }
                            }
                    )
                }

                "setCookie" -> {
                    if (!runtimeController.cookieManagerExtension.enabled) {
                        callResult.error(
                                "Invalid state",
                                "Cookie manager error not initialized",
                                "Cookie manager extension need to be enabled to manage cookies"
                        )
                        return
                    }

                    val domain = tryExtractOptionalSingleArgument<String>(call, "domain")
                    val expirationDate = tryExtractOptionalSingleArgument<Int>(call, "expirationDate")
                    val firstPartyDomain = tryExtractOptionalSingleArgument<String>(call, "firstPartyDomain")
                    val httpOnly = tryExtractOptionalSingleArgument<Boolean>(call, "httpOnly")
                    val name = tryExtractOptionalSingleArgument<String>(call, "name")
                    val partitionKey = tryExtractOptionalStructure(call, "partitionKey", CookiePartitionKey)
                    val path = tryExtractOptionalSingleArgument<String>(call, "path")

                    val sameSiteRaw = tryExtractOptionalSingleArgument<String>(call, "sameSite")
                    val sameSite = if (sameSiteRaw != null) CookieSameSiteStatus.valueOf(sameSiteRaw) else null

                    val secure = tryExtractOptionalSingleArgument<Boolean>(call, "secure")
                    val storeId = tryExtractOptionalSingleArgument<String>(call, "storeId")
                    val url = tryExtractSingleArgument<String>(call, "url")
                    val value = tryExtractOptionalSingleArgument<String>(call, "value")

                    runtimeController.cookieManagerExtension.setCookie(
                            domain, expirationDate, firstPartyDomain, httpOnly, name, partitionKey,
                            path, sameSite, secure, storeId, url, value,
                            object: ResultConsumer<Unit> {
                                override fun success(result: Unit) {
                                    callResult.success(true)
                                }

                                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                    callResult.error(errorCode, errorMessage, errorDetails)
                                }
                            }
                    )
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