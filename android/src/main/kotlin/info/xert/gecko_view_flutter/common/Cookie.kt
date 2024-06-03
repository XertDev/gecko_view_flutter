package info.xert.gecko_view_flutter.common

import org.json.JSONObject

enum class CookieSameSiteStatus(val value: String) {
    NO_RESTRICTION("no_restriction"),
    LAX("lax"),
    STRICT("strict"),
    UNSPECIFIED("")
}

data class CookiePartitionKey(
        val topLevelSite: String
) : OutputStructure, JSONOutputStructure {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        map["topLevelSite"] = topLevelSite

        return map
    }

    override fun toJSON(): JSONObject {
        val json = JSONObject()

        json.put("topLevelSite", topLevelSite)

        return json
    }

    companion object: JSONInputStructure<CookiePartitionKey>, InputStructure<CookiePartitionKey> {
        override fun fromJSON(inputJSON: JSONObject): CookiePartitionKey {
            return CookiePartitionKey(
                    topLevelSite = inputJSON.getString("topLevelSite")
            )
        }
        override fun fromMap(inputMap: Map<*, *>): CookiePartitionKey {
            return CookiePartitionKey(
                    topLevelSite = inputMap["topLevelSite"] as String
            )
        }
    }
}

data class Cookie(
        val domain: String,
        val expirationDate: Int?,
        val firstPartyDomain: String,
        val hostOnly: Boolean,
        val httpOnly: Boolean,
        val name: String,
        val partitionKey: CookiePartitionKey?,
        val path: String,
        val secure: Boolean,
        val session: Boolean,
        val sameSite: CookieSameSiteStatus,
        val storeId: String,
        val value: String
) : OutputStructure {
    override fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        map["domain"] = domain
        map["expirationDate"] = expirationDate
        map["firstPartyDomain"] = firstPartyDomain
        map["hostOnly"] = hostOnly
        map["httpOnly"] = httpOnly
        map["name"] = name
        map["partitionKey"] = partitionKey?.toMap()
        map["path"] = path
        map["secure"] = secure
        map["session"] = session
        map["sameSite"] = sameSite.value
        map["storeId"] = storeId
        map["value"] = value

        return map
    }

    companion object: JSONInputStructure<Cookie> {
        override fun fromJSON(inputJSON: JSONObject): Cookie {
            val partitionKeyRaw = toValueOrNull(inputJSON, "partitionKey") as JSONObject?
            val partitionKey = if (partitionKeyRaw == null || partitionKeyRaw.length() == 0) null
                               else CookiePartitionKey.fromJSON(partitionKeyRaw)

            return Cookie (
                    domain = inputJSON.getString("domain"),
                    expirationDate = toValueOrNull(inputJSON, "expirationDate") as Int?,
                    firstPartyDomain = inputJSON.getString("firstPartyDomain"),
                    hostOnly = inputJSON.getBoolean("hostOnly"),
                    httpOnly = inputJSON.getBoolean("httpOnly"),
                    name = inputJSON.getString("name"),
                    partitionKey = partitionKey,
                    path = inputJSON.getString("path"),
                    secure = inputJSON.getBoolean("secure"),
                    session = inputJSON.getBoolean("session"),
                    sameSite = CookieSameSiteStatus.entries.first { status -> status.value == inputJSON.getString("sameSite") },
                    storeId = inputJSON.getString("storeId"),
                    value = inputJSON.getString("value")
            )
        }
    }
}
