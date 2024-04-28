import 'package:flutter/material.dart';
import 'package:gecko_view_flutter/gecko_view_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  GeckoViewController? controller;
  GeckoTabController? tab;

  @override
  void initState() {
    super.initState();}

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: GeckoView(
          onGeckoViewCreated: (controller) async {
            this.controller = controller;
            tab = await this.controller?.createTab();
            tab?.activate();
            tab?.openURI(Uri.parse("https://www.w3schools.com/tags/tag_select.asp"));
          },
        ),
        bottomNavigationBar: BottomAppBar(
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              IconButton(
                icon: Icon(Icons.arrow_back),
                onPressed: () {
                  tab?.goBack();
                },
              ),
              IconButton(
                icon: Icon(Icons.refresh),
                onPressed: () {
                  tab?.reload();
                },
              ),
              IconButton(
                icon: Icon(Icons.arrow_forward),
                onPressed: () {
                  tab?.goForward();
                },
              )
            ]
          ),
        ),
      ),
    );
  }
}
