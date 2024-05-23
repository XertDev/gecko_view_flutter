enum ChoicePromptType {
  MENU(1),
  SINGLE(2),
  MULTI(3);

  const ChoicePromptType(this.value);

  final int value;
}

class Choice {
  final String id;
  final bool disabled;
  final String? icon;
  final List<Choice>? items;
  final String label;
  final bool selected;
  final bool separator;

  Choice(this.id, this.disabled, this.icon, this.items, this.label, this.selected, this.separator);

  Choice.fromMap(Map<Object?, Object?> map)
      : id = map["id"] as String,
        disabled = map["disabled"] as bool,
        icon = map["icon"] as String?,
        items = (map["items"] as List<Object?>?)?.map((item) => Choice.fromMap(item as Map<Object?, Object?>)).toList(),
        label = map["label"] as String,
        selected = map["selected"] as bool,
        separator = map["separator"] as bool;
}

class ChoicePromptRequest {
  final String? title;
  final String? message;
  final ChoicePromptType type;
  final List<Choice> choices;

  ChoicePromptRequest(this.title, this.message, this.type, this.choices);

  ChoicePromptRequest.fromMap(Map<Object?, Object?> map)
    : title = map["title"] as String?,
      message = map["message"] as String?,
      type = ChoicePromptType.values.firstWhere((element) => element.value == map["type"] as int),
      choices = (map["choices"] as List<Object?>).map((choice) => Choice.fromMap(choice as Map<Object?, Object?>)).toList();
}

class ChoicePromptResponse {
  bool confirmed;
  List<String> ids;

  ChoicePromptResponse.single(String id)
    : this.multiple([id]);

  ChoicePromptResponse.multiple(this.ids)
    : confirmed = true;

  ChoicePromptResponse.dismissed()
    : confirmed = false,
      ids = [];

  Map<String, dynamic> toMap() => {
    "confirmed": confirmed,
    "responses": ids
  };
}

class AlertPromptRequest {
  final String? title;
  final String? message;

  AlertPromptRequest(this.title, this.message);

  AlertPromptRequest.fromMap(Map<Object?, Object?> map)
    : title = map["title"] as String?,
      message = map["message"] as String?;
}

typedef ChoicePromptHandler = Future<ChoicePromptResponse> Function(ChoicePromptRequest);
typedef AlertPromptHandler = Future<void> Function(AlertPromptRequest);

class PromptHandler {
  ChoicePromptHandler onChoicePrompt = (_) async {
    throw UnimplementedError("Not implemented");
  };

  AlertPromptHandler onAlertPrompt = (_) async {
    throw UnimplementedError("Not implemented");
  };
}