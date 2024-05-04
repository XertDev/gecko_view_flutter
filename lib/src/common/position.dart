class GeckoPosition {
  int x;
  int y;

  GeckoPosition(this.x, this.y);

  GeckoPosition.fromMap(Map<Object?, Object?> map)
      : x = map["x"] as int,
        y = map["y"] as int;

  Map<String, dynamic> toMap() => {
    "x": x,
    "y": y
  };
}

class GeckoOffset {
  int x;
  int y;

  GeckoOffset(this.x, this.y);

  GeckoOffset.fromMap(Map<Object?, Object?> map)
      : x = map["x"] as int,
        y = map["y"] as int;

  Map<String, dynamic> toMap() => {
    "x": x,
    "y": y
  };
}