import 'package:flutter/material.dart';
import 'package:gecko_view_flutter/gecko_view_flutter.dart';
import 'package:gecko_view_flutter_example/utils.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await GeckoViewFlutter.enableHostJSExecution();
  await GeckoViewFlutter.enableCookieManager();

  runApp(const MaterialApp(
    home: MyApp(),
  ));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

const TEST_COOKIE = "TEST";

class _MyAppState extends State<MyApp> {
  late GeckoCookieManager cookieManager;
  GeckoViewController? controller;
  GeckoTabController? tab;

  @override
  void initState() {
    cookieManager = GeckoViewFlutter.cookieManager();
    super.initState();
  }

  void showInfoToast(BuildContext context, String message)  {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(message),
    ));
  }

  void showOperationFailedToast(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text('Failed to perform operation: $message'),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: GeckoView(
        onGeckoViewCreated: (controller) async {
          this.controller = controller;
          tab = await this.controller?.createTab();
          tab?.activate();
          await tab?.openURI(Uri.parse('https://dart.dev/'));
        },
      ),
      bottomNavigationBar: BottomAppBar(
        child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [
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
        ]),
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
            ListTile(
                title: const Text('List cookies for current domain'),
                onTap: () async {
                  final fullUrl = await tab?.currentUrl();
                  if (fullUrl != null) {
                    final baseURL = extractBaseURLString(fullUrl);
                    final cookies = await cookieManager.getAllCookies(url: baseURL);
                    await showDialog(
                      context: context,
                      builder: (BuildContext context) =>
                          SimpleDialog(
                              title: const Text("Cookies"),
                              children: [
                                SingleChildScrollView(
                                  child: ListBody(
                                    children: cookies.map((cookie) =>
                                        ListTile(
                                            title: Text(cookie.name)
                                        )
                                    ).toList(),
                                  ),
                                )
                              ]
                          ),
                    );
                  }
                }
            ),
            ListTile(
              title: const Text("Get cookie test"),
              onTap: () async {
                final fullUrl = await tab?.currentUrl();
                if (fullUrl != null) {
                  final baseURL = extractBaseURLString(fullUrl);
                  try {
                    final cookie = await cookieManager.getCookie(
                        name: TEST_COOKIE, url: baseURL);
                    await showDialog(
                      context: context,
                      builder: (BuildContext context) => SimpleDialog(
                          title: const Text("Cookies"),
                          children: [
                            SingleChildScrollView(
                              child: ListBody(
                                  children: [
                                    ListTile(
                                      title: Text("${cookie.name}:${cookie.value}"),
                                    )
                                  ]
                              ),
                            )
                          ]
                      ),
                    );
                  } on CookieException catch (e) {
                    showOperationFailedToast(context, e.message);
                  }
                }
              },
            ),
            ListTile(
              title: const Text("Set cookie test"),
              onTap: () async {
                final fullUrl = await tab?.currentUrl();
                if (fullUrl != null) {
                  final baseURL = extractBaseURLString(fullUrl);
                  try {
                    await cookieManager.setCookie(
                        url: baseURL, name: TEST_COOKIE, value: TEST_COOKIE);
                  } on CookieException catch (e) {
                    showOperationFailedToast(context, e.message);
                  }
                }
              },
            ),
            ListTile(
              title: const Text("Clear cookie test"),
              onTap: () async {
                final fullUrl = await tab?.currentUrl();
                if (fullUrl != null) {
                  final baseURL = extractBaseURLString(fullUrl);
                  try {
                    await cookieManager.removeCookie(name: TEST_COOKIE, url: baseURL);
                  } on CookieException catch (e) {
                    showOperationFailedToast(context, e.message);
                  }
                }
              },
            ),
            ListTile(
              title: const Text("Find"),
              onTap: () async {
                final activeTab = await controller?.getActiveTab();
                final findController = activeTab?.findController();
                final result = await findController?.find(const GeckoFindRequest(
                  searchString: "isolate",
                  drawLinkOutline: true,
                  highlightAll: true
                ));
                showInfoToast(context,
                    "Found ${result?.total} occurrences."
                    " Moving to ${result?.occurrenceOffset}."
                    " Wrapped: ${result?.wrapped}"
                );
              }
            ),
            ListTile(
              title: const Text("Clear find"),
              onTap: () async {
                final activeTab = await controller?.getActiveTab();
                final findController = activeTab?.findController();
                await findController?.clear();
              },
            )
          ],
        ),
      ),
    );
  }
}
