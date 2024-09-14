package info.xert.gecko_view_flutter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import info.xert.gecko_view_flutter.common.FindDirection
import info.xert.gecko_view_flutter.common.Offset
import info.xert.gecko_view_flutter.common.Position
import info.xert.gecko_view_flutter.common.ResultConsumer
import info.xert.gecko_view_flutter.common.FindRequest
import info.xert.gecko_view_flutter.common.FindResult
import info.xert.gecko_view_flutter.delegate.FlutterPromptDelegate
import info.xert.gecko_view_flutter.delegate.FlutterNavigationDelegate
import info.xert.gecko_view_flutter.delegate.FlutterScrollDelegate
import org.json.JSONObject
import org.mozilla.geckoview.GeckoResult

import org.mozilla.geckoview.GeckoSession

import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.PanZoomController.SCROLL_BEHAVIOR_AUTO
import org.mozilla.geckoview.PanZoomController.SCROLL_BEHAVIOR_SMOOTH
import org.mozilla.geckoview.ScreenLength
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtension.MessageDelegate

internal class GeckoViewInstance(context: Context,
                                 private val runtimeController: GeckoRuntimeController,
                                 private val proxy: GeckoViewProxy) {

    private val TAG: String = GeckoViewInstance::class.java.name

    private val viewContext: Context

    val view: GeckoView

    data class SessionWrapper(val session: GeckoSession, var tabId: Int?)
    private val sessions: MutableMap<Int, SessionWrapper> = HashMap()


    init {
        Log.d(TAG, "Initializing GeckoView")

        viewContext = context

        view = GeckoView(viewContext)
    }

    fun init() {
    }

    fun createTab(tabId: Int) {
        if(sessions.containsKey(tabId)) {
            throw InternalError("Internal tab id reused")
        }

        val session = GeckoSession();
        sessions[tabId] = SessionWrapper(session, null)

        session.promptDelegate = FlutterPromptDelegate(proxy)
        session.scrollDelegate = FlutterScrollDelegate()
        session.navigationDelegate = FlutterNavigationDelegate()

        session.permissionDelegate = object : GeckoSession.PermissionDelegate {}

        if (runtimeController.tabDataInitializer.enabled) {
            val extension = runtimeController.tabDataInitializer.extension
            if (extension != null) {
                Handler(Looper.getMainLooper()).post {
                    session.webExtensionController.setMessageDelegate(extension, object : MessageDelegate {
                        override fun onMessage(nativeApp: String, message: Any, sender: WebExtension.MessageSender): GeckoResult<Any>? {
                            val sessionWrapper = sessions[tabId]!!
                            if (sessionWrapper.tabId == null) {
                                val jsonMessage = message as JSONObject

                                if (jsonMessage.getString("type") == "tabId") {
                                    sessionWrapper.tabId = jsonMessage.getInt("value")
                                }
                            }
                            return null
                        }
                    }, "browser")
                }
            }
        }

        session.open(runtimeController.getRuntime());
    }

    fun getActiveTabId(): Int? {
        val tabEntry = sessions.entries.firstOrNull { entry -> entry.value.session == view.session }
        if (tabEntry != null) {
            return tabEntry.key
        }

        return null
    }

    private fun getSessionByTabId(tabId: Int): GeckoSession {
        if(!sessions.containsKey(tabId)) {
            throw InternalError("Tab does not exist")
        }

        return sessions[tabId]!!.session
    }

    private fun getInternalTabIdByTabId(tabId: Int): Int? {
        if(!sessions.containsKey(tabId)) {
            throw InternalError("Tab does not exist")
        }

        return sessions[tabId]!!.tabId
    }


    fun isTabActive(tabId: Int): Boolean {
        val session = getSessionByTabId(tabId)
        return session == view.session
    }

    fun activateTab(tabId: Int) {
        val session = getSessionByTabId(tabId)
        view.setSession(session)
    }

    fun currentUrl(tabId: Int): String? {
        val session = getSessionByTabId(tabId)
        val navigation = session.navigationDelegate
        if(navigation is FlutterNavigationDelegate) {
            return navigation.currentUrl
        } else {
            throw InternalError("Invalid session")
        }
    }

    fun getUserAgent(tabId: Int): String? {
        val session = getSessionByTabId(tabId)
        return session.userAgent.poll()
    }

    fun openURI(tabId: Int, uri: String) {
        val session = getSessionByTabId(tabId)
        session.loadUri(uri)
    }

    fun reload(tabId: Int) {
        val session = getSessionByTabId(tabId)
        session.reload()
    }

    fun goBack(tabId: Int) {
        val session = getSessionByTabId(tabId)
        session.goBack()
    }

    fun goForward(tabId: Int) {
        val session = getSessionByTabId(tabId)
        session.goForward()
    }

    fun getScrollOffset(tabId: Int): Offset {
        val session = getSessionByTabId(tabId)
        val scroll = session.scrollDelegate
        if(scroll is FlutterScrollDelegate) {
            return scroll.scrollOffset
        } else {
            throw InternalError("Invalid session")
        }
    }

    fun scrollToBottom(tabId: Int) {
        val session = getSessionByTabId(tabId)
        session.panZoomController.scrollToBottom()
    }

    fun scrollToTop(tabId: Int) {
        val session = getSessionByTabId(tabId)
        session.panZoomController.scrollToTop()
    }

    fun scrollBy(tabId: Int, offset: Offset, smooth: Boolean) {
        val session = getSessionByTabId(tabId)
        session.panZoomController.scrollBy(
                ScreenLength.fromPixels(offset.x.toDouble()),
                ScreenLength.fromPixels(offset.y.toDouble()),
                if (smooth) SCROLL_BEHAVIOR_SMOOTH else SCROLL_BEHAVIOR_AUTO)
    }

    fun scrollTo(tabId: Int, position: Position, smooth: Boolean) {
        val session = getSessionByTabId(tabId)
        session.panZoomController.scrollTo(
                ScreenLength.fromPixels(position.x.toDouble()),
                ScreenLength.fromPixels(position.y.toDouble()),
                if (smooth) SCROLL_BEHAVIOR_SMOOTH else SCROLL_BEHAVIOR_AUTO)
    }

    fun runJsAsync(tabId: Int, script: String) {
        val browserTabId = getInternalTabIdByTabId(tabId)
                ?: throw InternalError("Invalid session state! TabId not initialized");
        runtimeController.hostJsExecutor.runAsync(script, browserTabId)
    }

    fun findNext(tabId: Int, request: FindRequest, callback: ResultConsumer<FindResult>) {
        val session = getSessionByTabId(tabId)
        val finder = session.finder

        var searchFlags: Int = 0
        if (request.direction == FindDirection.BACKWARDS) {
            searchFlags = searchFlags or GeckoSession.FINDER_FIND_BACKWARDS
        }
        if (request.matchCase) {
            searchFlags = searchFlags or GeckoSession.FINDER_FIND_MATCH_CASE
        }
        if (request.wholeWord) {
            searchFlags = searchFlags or GeckoSession.FINDER_FIND_WHOLE_WORD
        }
        if (request.linksOnly) {
            searchFlags = searchFlags or GeckoSession.FINDER_FIND_LINKS_ONLY
        }

        var displayFlags: Int = 0
        if (request.highlightAll) {
            displayFlags = displayFlags or GeckoSession.FINDER_DISPLAY_HIGHLIGHT_ALL
        }
        if (request.dimPage) {
            displayFlags = displayFlags or GeckoSession.FINDER_DISPLAY_DIM_PAGE
        }
        if (request.drawLinkOutline) {
            displayFlags = displayFlags or GeckoSession.FINDER_DISPLAY_DRAW_LINK_OUTLINE
        }

        finder.displayFlags = displayFlags
        finder.find(request.searchString, searchFlags).accept (
            { result ->
                if (result != null) {
                    callback.success(FindResult(
                        result.found, result.current, result.linkUri.orEmpty(), result.total, result.wrapped
                    ))
                } else {
                    callback.success(
                        FindResult(
                        false, 0, "", 0, false
                    )
                    )
                }
            },
            {
                e ->
                    Log.e(TAG, "Failed to perform find")
                callback.error(TAG, "Failed to perform find", e)
            }
        )
    }

    fun findClear(tabId: Int) {
        val session = getSessionByTabId(tabId)
        val finder = session.finder

        finder.clear()
    }
}