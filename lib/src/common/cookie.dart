enum CookieSameSiteStatus {
  NO_RESTRICTION("no_restriction"),
  LAX("lax"),
  STRICT("strict"),
  UNSPECIFIED("");

  const CookieSameSiteStatus(this.value);

  final String value;
}

class CookiePartitionKey {
  String topLevelSite;

  CookiePartitionKey(this.topLevelSite);

  CookiePartitionKey.fromMap(Map<Object?, Object?> map)
      : topLevelSite = map["topLevelSite"] as String;

  Map<String, dynamic> toMap() => {
    "topLevelSite": topLevelSite
  };
}

class Cookie {
  String domain;
  int? expirationDate;
  String firstPartyDomain;
  bool hostOnly;
  bool httpOnly;
  String name;
  CookiePartitionKey? partitionKey;
  String path;
  bool secure;
  bool session;
  CookieSameSiteStatus sameSite;
  String storeId;
  String value;


  Cookie(
      this.domain,
      this.expirationDate,
      this.firstPartyDomain,
      this.hostOnly, this.httpOnly,
      this.name,
      this.partitionKey,
      this.path,
      this.secure,
      this.session,
      this.sameSite,
      this.storeId,
      this.value);

  Cookie.fromMap(Map<Object?, Object?> map)
      : domain = map["domain"] as String,
        expirationDate = map["expirationDate"] as int?,
        firstPartyDomain = map["firstPartyDomain"] as String,
        hostOnly = map["hostOnly"] as bool,
        httpOnly = map["httpOnly"] as bool,
        name = map["name"] as String,
        partitionKey = ((map["partitionKey"] as Map<Object?, Object?>?) != null ?
        CookiePartitionKey.fromMap((map["partitionKey"] as Map<Object?, Object?>?)!)
            : null),
        path = map["path"] as String,
        secure = map["secure"] as bool,
        session = map["session"] as bool,
        sameSite = CookieSameSiteStatus.values
            .firstWhere((element) => element.value == map["sameSite"] as String),
        storeId = map["storeId"] as String,
        value = map["value"] as String;

  Map<String, dynamic> toMap() => {
    "domain": domain,
    "expirationDate": expirationDate,
    "firstPartyDomain": firstPartyDomain,
    "hostOnly": hostOnly,
    "httpOnly": httpOnly,
    "name": name,
    "partitionKey": partitionKey?.toMap(),
    "path": path,
    "secure": secure,
    "session": session,
    "sameSite": sameSite.value,
    "storeId": storeId,
    "value": value
  };
}