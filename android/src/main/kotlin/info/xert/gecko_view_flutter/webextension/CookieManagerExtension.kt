package info.xert.gecko_view_flutter.webextension

import info.xert.gecko_view_flutter.common.Cookie
import info.xert.gecko_view_flutter.common.CookiePartitionKey
import info.xert.gecko_view_flutter.common.CookieSameSiteStatus
import info.xert.gecko_view_flutter.common.ResultConsumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.mozilla.geckoview.WebExtension

class CookieManagerExtension: Extension() {
    override val TAG: String
        get() = CookieManagerExtension::class.java.name
    override val extensionID: String
        get() = "cookie-managern@xert.info"
    override val extensionPath: String
        get() = "assets/webextension/cookie_manager/"

    private var nextRequestId: Int = 0
    private val requestHandlers = HashMap<Int, ResultConsumer<JSONObject>>()
    private val mutex = Mutex()

    init {
        messageHandler = fun(message: Any) {
            runBlocking {
                withContext(Dispatchers.Default) {
                    mutex.withLock {
                        val messageJSON = message as JSONObject;
                        val requestId = messageJSON.getInt("id")
                        val status = messageJSON.getString("status")
                        if (status == "success") {
                            requestHandlers[requestId]?.success(message)
                        } else {
                            requestHandlers[requestId]?.error(
                                    "Cookie Manager",
                                    "Failed to perform operation",
                                    message.getString("error")
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleRequest(command: String, args: JSONObject, callback: ResultConsumer<JSONObject>) {
        val message = JSONObject()
        message.put("action", command);
        message.put("args", args)

        runBlocking {
            withContext(Dispatchers.Default) {
                mutex.withLock {
                    message.put("id", nextRequestId)

                    requestHandlers[nextRequestId] = callback

                    nextRequestId += 1

                    port?.postMessage(message)
                }
            }
        }
    }
    fun getCookie(
            firstPartyDomain: String?,
            name: String,
            partitionKey: CookiePartitionKey?,
            storeId: String?,
            url: String,
            callback: ResultConsumer<Cookie>
    ) {
        if (port != null) {
            val args = JSONObject()

            if (firstPartyDomain == null) {
                args.put("firstPartyDomain", JSONObject.NULL)
            } else {
                args.put("firstPartyDomain", firstPartyDomain)
            }
            args.put("name", name)

            if (partitionKey == null) {
                args.put("partitionKey", JSONObject.NULL)
            } else {
                args.put("partitionKey", partitionKey.toJSON())
            }

            if (storeId == null) {
                args.put("storeId", JSONObject.NULL)
            } else {
                args.put("storeId", storeId)
            }

            args.put("url", url)

            scheduleRequest("get", args, object: ResultConsumer<JSONObject> {
                override fun success(result: JSONObject) {
                    callback.success(Cookie.fromJSON(result.getJSONObject("result")))
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    callback.error(errorCode, errorMessage, errorDetails)
                }

            })

        } else {
            throw InternalError("Host JS Execution Extension not initialized")
        }
    }

    fun getAllCookie(
            domain: String?,
            firstPartyDomain: String?,
            name: String?,
            partitionKey: CookiePartitionKey?,
            storeId: String?,
            url: String,
            callback: ResultConsumer<List<Cookie>>
    ) {
        if (port != null) {
            val args = JSONObject()

            if (domain == null) {
                args.put("domain", JSONObject.NULL)
            } else {
                args.put("domain", domain)
            }

            if (firstPartyDomain == null) {
                args.put("firstPartyDomain", JSONObject.NULL)
            } else {
                args.put("firstPartyDomain", firstPartyDomain)
            }

            args.put("name", name)

            if (partitionKey == null) {
                args.put("partitionKey", JSONObject.NULL)
            } else {
                args.put("partitionKey", partitionKey.toJSON())
            }

            if (storeId == null) {
                args.put("storeId", JSONObject.NULL)
            } else {
                args.put("storeId", storeId)
            }

            args.put("url", url)

            scheduleRequest("getAll", args, object: ResultConsumer<JSONObject> {
                override fun success(result: JSONObject) {
                    val jsonArray = result.getJSONArray("result")
                    val cookies: MutableList<Cookie> = mutableListOf()

                    repeat(jsonArray.length()) {
                        index ->
                            cookies.add(Cookie.fromJSON(jsonArray.getJSONObject(index)))
                    }

                    callback.success(cookies)
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    callback.error(errorCode, errorMessage, errorDetails)
                }

            })

        } else {
            throw InternalError("Host JS Execution Extension not initialized")
        }
    }

    fun removeCookie(
            firstPartyDomain: String?,
            name: String,
            partitionKey: CookiePartitionKey?,
            storeId: String?,
            url: String,
            callback: ResultConsumer<Unit>) {
        if (port != null) {
            val args = JSONObject()

            if (firstPartyDomain == null) {
                args.put("firstPartyDomain", JSONObject.NULL)
            } else {
                args.put("firstPartyDomain", firstPartyDomain)
            }

            args.put("name", name)

            if (partitionKey == null) {
                args.put("partitionKey", JSONObject.NULL)
            } else {
                args.put("partitionKey", partitionKey.toJSON())
            }

            if (storeId == null) {
                args.put("storeId", JSONObject.NULL)
            } else {
                args.put("storeId", storeId)
            }

            args.put("url", url)

            scheduleRequest("remove", args, object: ResultConsumer<JSONObject> {
                override fun success(result: JSONObject) {
                    callback.success(Unit)
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    callback.error(errorCode, errorMessage, errorDetails)
                }
            })

        } else {
            throw InternalError("Host JS Execution Extension not initialized")
        }
    }

    fun setCookie(
            domain: String?,
            expirationDate: Int?,
            firstPartyDomain: String?,
            httpOnly: Boolean?,
            name: String?,
            partitionKey: CookiePartitionKey?,
            path: String?,
            sameSite: CookieSameSiteStatus?,
            secure: Boolean?,
            storeId: String?,
            url: String,
            value: String?,
            callback: ResultConsumer<Unit>) {
        if (port != null) {
            val args = JSONObject()

            if (domain == null) {
                args.put("domain", JSONObject.NULL)
            } else {
                args.put("domain", domain)
            }

            if (expirationDate == null) {
                args.put("expirationDate", JSONObject.NULL)
            } else {
                args.put("expirationDate", domain)
            }

            if (firstPartyDomain == null) {
                args.put("firstPartyDomain", JSONObject.NULL)
            } else {
                args.put("firstPartyDomain", firstPartyDomain)
            }

            if (httpOnly == null) {
                args.put("httpOnly", JSONObject.NULL)
            } else {
                args.put("httpOnly", httpOnly)
            }

            if (name == null) {
                args.put("name", JSONObject.NULL)
            } else {
                args.put("name", name)
            }

            if (partitionKey == null) {
                args.put("partitionKey", JSONObject.NULL)
            } else {
                args.put("partitionKey", partitionKey.toJSON())
            }

            if (path == null) {
                args.put("path", JSONObject.NULL)
            } else {
                args.put("path", path)
            }

            if (sameSite == null) {
                args.put("sameSite", JSONObject.NULL)
            } else {
                args.put("sameSite", sameSite.value)
            }

            if (secure == null) {
                args.put("secure", JSONObject.NULL)
            } else {
                args.put("secure", secure)
            }

            if (storeId == null) {
                args.put("storeId", JSONObject.NULL)
            } else {
                args.put("storeId", storeId)
            }

            args.put("url", url)

            if (value == null) {
                args.put("value", JSONObject.NULL)
            } else {
                args.put("value", value)
            }

            scheduleRequest("set", args, object: ResultConsumer<JSONObject> {
                override fun success(result: JSONObject) {
                    callback.success(Unit)
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    callback.error(errorCode, errorMessage, errorDetails)
                }
            })
        } else {
            throw InternalError("Host JS Execution Extension not initialized")
        }
    }
}