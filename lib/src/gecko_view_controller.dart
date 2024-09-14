import 'common/find_request.dart';
import 'common/find_response.dart';
import 'common/position.dart';
import 'delegate/prompt_delegate.dart';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

import 'host/method_channel/method_channel_proxy.dart';
import 'host/prompt_handler.dart';

typedef GeckoViewCreatedCallback = void Function(
    GeckoViewController controller);

class GeckoView extends StatefulWidget {
  const GeckoView({
    super.key,
    required this.onGeckoViewCreated
  });

  final GeckoViewCreatedCallback onGeckoViewCreated;

  @override
  State<GeckoView> createState() => _GeckoViewState();
}

class _GeckoViewState extends State<GeckoView>
    with WidgetsBindingObserver {
  final String viewType = 'gecko_view';
  late GeckoViewController controller;

  @override
  void initState() {
    WidgetsBinding.instance.addObserver(this);
    super.initState();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  void _onPlatformViewCreated(int id) {
    controller = GeckoViewController._(context, id);
    widget.onGeckoViewCreated(controller);
  }

  @override
  Widget build(BuildContext context) {
    const Map<String, dynamic> creationParams = <String, dynamic>{};

    return PlatformViewLink(
        surfaceFactory: (context, controller) {
          return AndroidViewSurface(
              controller: controller as AndroidViewController,
              gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque
          );
        },
        onCreatePlatformView: (params) {
          return PlatformViewsService.initSurfaceAndroidView(
              id: params.id,
              viewType: params.viewType,
              layoutDirection: TextDirection.ltr,
              creationParams: creationParams,
              creationParamsCodec: const StandardMessageCodec(),
              onFocus: () {
                params.onFocusChanged(true);
              }
          )
            ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
            ..addOnPlatformViewCreatedListener(_onPlatformViewCreated)
            ..create();
        },
        viewType: viewType
    );
  }
}

class GeckoJavascriptController {
  final int _viewId;
  final int _tabId;

  GeckoJavascriptController._(
      this._viewId,
      this._tabId
      );

  Future<void> runAsync(String script) async {
    await MethodChannelProxy.instance.runJSAsync(_viewId, _tabId, script);
  }
}

class GeckoFindController {
  final int _viewId;
  final int _tabId;

  GeckoFindController._(
      this._viewId,
      this._tabId
  );

  Future<GeckoFindResult> find(GeckoFindRequest request) async {
    return await MethodChannelProxy.instance.findNext(_viewId, _tabId, request);
  }

  Future<void> clear() async {
    await MethodChannelProxy.instance.findClear(_viewId, _tabId);
  }
}

class GeckoTabController {
  final int _viewId;
  final int _tabId;

  late final GeckoJavascriptController _javascriptController;
  late final GeckoFindController _findController;

  GeckoTabController._(
      this._viewId,
      this._tabId
      ) {
    _javascriptController = GeckoJavascriptController._(_viewId, _tabId);
    _findController = GeckoFindController._(_viewId, _tabId);
  }

  int id() {
    return _tabId;
  }

  GeckoJavascriptController javascriptController() {
    return _javascriptController;
  }

  GeckoFindController findController() {
    return _findController;
  }

  Future<bool> isActive() async {
    return await MethodChannelProxy.instance.isTabActive(_viewId, _tabId);
  }

  Future<void> activate() async {
    await MethodChannelProxy.instance.activateTab(_viewId, _tabId);
  }

  Future<String?> currentUrl() async {
    return await MethodChannelProxy.instance.getCurrentUrl(_viewId, _tabId);
  }

  Future<String?> getTitle() async {
    return await MethodChannelProxy.instance.getTitle(_viewId, _tabId);
  }

  Future<String?> getUserAgent() async {
    return await MethodChannelProxy.instance.getUserAgent(_viewId, _tabId);
  }

  Future<void> openURI(Uri uri) async {
    await MethodChannelProxy.instance.openURI(_viewId, _tabId, uri);
  }

  Future<void> reload() async {
    await MethodChannelProxy.instance.reload(_viewId, _tabId);
  }

  Future<void> goBack() async {
    await MethodChannelProxy.instance.goBack(_viewId, _tabId);
  }

  Future<void> goForward() async {
    await MethodChannelProxy.instance.goForward(_viewId, _tabId);
  }

  Future<GeckoOffset> getScrollOffset() async {
    return await MethodChannelProxy.instance.getScrollOffset(_viewId, _tabId);
  }

  Future<void> scrollToBottom() async {
    await MethodChannelProxy.instance.scrollToBottom(_viewId, _tabId);
  }

  Future<void> scrollToTop() async {
    await MethodChannelProxy.instance.scrollToTop(_viewId, _tabId);
  }

  Future<void> scrollBy(GeckoOffset offset, bool smooth) async {
    await MethodChannelProxy.instance.scrollBy(_viewId, _tabId, offset, smooth);
  }

  Future<void> scrollTo(GeckoPosition position, bool smooth) async {
    await MethodChannelProxy.instance.scrollTo(_viewId, _tabId, position, smooth);
  }
}

class GeckoViewController {
  BuildContext _context;

  final int _id;

  int _nextTabId = 0;
  final List<GeckoTabController> _tabs = [];

  late final PromptHandler _promptHandler;

  PromptDelegate promptDelegate = FlutterPromptDelegate();

  GeckoViewController._(
      this._context,
      this._id
  ) {
    init();
    _promptHandler = initPromptHandler();
  }

  Future<void> init() async {
    await MethodChannelProxy.instance.register(_id);
  }

  PromptHandler initPromptHandler() {
    final handler = MethodChannelProxy.instance.registerPromptHandler(_id);
    handler.onChoicePrompt = onChoicePrompt;
    handler.onAlertPrompt = onAlertPrompt;

    return handler;
  }

  Future<GeckoTabController> createTab() async {
    final int tabId = _nextTabId;
    ++_nextTabId;

    await MethodChannelProxy.instance.createTab(_id, tabId);
    final tab = GeckoTabController._(_id, tabId);
    _tabs.add(tab);
    return tab;
  }

  Future<GeckoTabController?> getActiveTab() async {
    final activeId = await MethodChannelProxy.instance.getActiveTab(_id);
    if (activeId != null) {
      return _tabs.firstWhere((tab) => tab.id() == activeId);
    }

    return null;
  }

  Future<ChoicePromptResponse> onChoicePrompt(ChoicePromptRequest request) async {
    return await promptDelegate.onChoicePrompt(_context, request);
  }

  Future<void> onAlertPrompt(AlertPromptRequest request) async {
    return await promptDelegate.onAlertPrompt(_context, request);
  }
}