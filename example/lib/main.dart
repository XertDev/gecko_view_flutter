import 'package:flutter/material.dart';
import 'package:gecko_view_flutter/gecko_view_flutter.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await GeckoViewFlutter.enableHostJSExecution();

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
    super.initState();
  }

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
            await this.controller?.createTab();
            await this.controller?.createTab();
            tab = await this.controller?.createTab();
            tab?.activate();
            await tab?.openURI(Uri.parse('https://dart.dev/'));
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
        drawer: Drawer(
          child: ListView(
            // Important: Remove any padding from the ListView.
            padding: EdgeInsets.zero,
            children: [
              const DrawerHeader(
                decoration: BoxDecoration(
                  color: Colors.blue,
                ),
                child: Text('Additional features'),
              ),
              ListTile(
                title: const Text('Run JS async'),
                onTap: () async {
                  final activeTab = await controller?.getActiveTab();
                  final javascriptController = activeTab?.javascriptController();
                  javascriptController?.runAsync('alert("test")');
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
