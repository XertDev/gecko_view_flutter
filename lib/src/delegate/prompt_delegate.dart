import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:gecko_view_flutter/src/host/prompt_handler.dart';

abstract class PromptDelegate {
  Future<ChoicePromptResponse> onChoicePrompt(BuildContext context, ChoicePromptRequest request);
  Future<void> onAlertPrompt(BuildContext context, AlertPromptRequest request);
}

class FlutterPromptDelegate extends PromptDelegate {
  Widget createChoicePromptSingleItem(Choice item, int level, void Function(String) confirm) {
    return ListTile(
      title: Text(item.label),

      enabled: !item.disabled,
      selected: item.selected,
      onTap: () {
        confirm(item.id);
      },
      trailing: Icon(item.selected ? Icons.radio_button_checked : Icons.radio_button_unchecked),
    );
  }

  Widget createChoicePromptSingleGroup(Choice item, int level) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Text(
          item.label
      ),
    );
  }

  List<Widget> createChoicePromptOptions(ChoicePromptType type, List<Choice> items, int level, void Function(String) confirm) {
    final options = List<Widget>.empty(growable: true);
    if(type == ChoicePromptType.MENU) {
      //not supported
      return options;
    }

    for(final item in items) {
      if(item.items != null) {
        options.add(createChoicePromptSingleGroup(item, level));
        options.addAll(createChoicePromptOptions(type, item.items!, level+1, confirm));
      } else {
        options.add(createChoicePromptSingleItem(item, level, confirm));
      }
    }
    
    return options;
  }

  Future<ChoicePromptResponse> onSingleChoicePrompt(BuildContext context, ChoicePromptRequest request) async {
    final contentElements = List<Widget>.empty(growable: true);
    if (request.message != null) {
      contentElements.add(Text(request.message!));
    }
    
    contentElements.add(SingleChildScrollView(
      child: ListBody(
        children: createChoicePromptOptions(request.type, request.choices, 0, (optionId) {
          Navigator.of(context).pop(optionId);
        }),
      )
    ));

    final userResponse = await showDialog<String>(
      context: context,
      builder: (context) {
        return SimpleDialog(
            title: request.title != null ? Text(request.title!) : null,
            children: contentElements
        );
      },
    );

    if(userResponse == null) {
      return ChoicePromptResponse.dismissed();
    } else {
      return ChoicePromptResponse.single(userResponse);
    }
  }

  @override
  Future<ChoicePromptResponse> onChoicePrompt(BuildContext context, ChoicePromptRequest request) async {
    switch (request.type) {
      case ChoicePromptType.SINGLE:
        return onSingleChoicePrompt(context, request);
      default:
        return ChoicePromptResponse.dismissed();
    }
  }

  @override
  Future<void> onAlertPrompt(BuildContext context, AlertPromptRequest request) async {
    await showDialog<void>(
      context: context,
      builder: (context) {
        List<String> lines = List.empty();
        if(request.message != null) {
          const LineSplitter splitter = LineSplitter();
          lines = splitter.convert(request.message!);
        }

        return AlertDialog(
            title: request.title != null ? Text(request.title!) : null,
            content: SingleChildScrollView(
              child: ListBody(
                children: lines.map((line) => Text(line)).toList(),
              ),
            ),
          actions: [
            TextButton(
              child: const Text("Dismiss"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            )
          ],
        );
      },
    );
  }
}