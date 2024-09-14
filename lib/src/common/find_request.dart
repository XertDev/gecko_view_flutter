enum GeckoFindDirection {
  FORWARD("forward"),
  BACKWARDS("backwards");

  const GeckoFindDirection(this.value);

  final String value;
}

class GeckoFindRequest {
  final String searchString;
  final GeckoFindDirection direction;
  final bool matchCase;
  final bool wholeWord;
  final bool linksOnly;
  final bool highlightAll;
  final bool dimPage;
  final bool drawLinkOutline;

  const GeckoFindRequest({
    required this.searchString,
    this.direction = GeckoFindDirection.FORWARD,
    this.matchCase = false,
    this.wholeWord = false,
    this.linksOnly = false,
    this.highlightAll = false,
    this.dimPage = false,
    this.drawLinkOutline = false
  });

  Map<String, dynamic> toMap() => {
    "searchString": searchString,
    "direction": direction.value,

    "matchCase": matchCase,
    "wholeWord": wholeWord,
    "linksOnly": linksOnly,
    "highlightAll": highlightAll,
    "dimPage": dimPage,
    "drawLinkOutline": drawLinkOutline
  };
}