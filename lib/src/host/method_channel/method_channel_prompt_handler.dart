import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:gecko_view_flutter/src/host/prompt_handler.dart';

class MethodChannelPromptHandler extends PromptHandler{
  final MethodChannel _channel;

  MethodChannelPromptHandler(
      this._channel,
  ) : super() {
    _channel.setMethodCallHandler((call) async {
      switch(call.method) {
        case "choicePrompt":
          try {
            final request = ChoicePromptRequest.fromMap(
                call.arguments as Map<Object?, Object?>);
            final response = await onChoicePrompt(request);
            return response.toMap();
          }
          catch(e) {
            debugPrint(e.toString());
          }

        case "alertPrompt":
          try {
            final request = AlertPromptRequest.fromMap(
                call.arguments as Map<Object?, Object?>);
            await onAlertPrompt(request);
          }
          catch(e) {
            debugPrint(e.toString());
          }
      }
    });
  }
}