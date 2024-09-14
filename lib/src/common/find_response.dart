class GeckoFindResult {
  final bool matched;
  final int occurrenceOffset;
  final String linkUri;

  final int total;
  final bool wrapped;

  const GeckoFindResult({
    required this.matched,
    required this.occurrenceOffset,
    required this.linkUri,

    required this.total,
    required this.wrapped
  });

  GeckoFindResult.fromMap(Map<Object?, Object?> map)
    : matched = map["matched"] as bool,
      occurrenceOffset = map["occurrenceOffset"] as int,
      linkUri = map["linkUri"] as String,
      total = map["total"] as int,
      wrapped = map["wrapped"] as bool;
}