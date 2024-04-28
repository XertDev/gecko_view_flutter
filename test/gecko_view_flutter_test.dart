import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gecko_view_flutter/gecko_view_flutter.dart';

void main() {
  const MethodChannel channel = MethodChannel("gecko_view_flutter");

  handler(MethodCall methodCall) async {
    return "42";
  }

  TestWidgetsFlutterBinding.ensureInitialized();
  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
    .setMockMethodCallHandler(channel, handler);

  test("getPlatformVersion", () async {
    expect(await GeckoViewFlutter.platformVersion, "42");
  });
}