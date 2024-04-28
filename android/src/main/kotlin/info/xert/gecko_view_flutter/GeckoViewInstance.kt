package info.xert.gecko_view_flutter

import android.content.Context
import android.util.Log
import android.view.View
import info.xert.gecko_view_flutter.delegate.FlutterPromptDelegate
import info.xert.gecko_view_flutter.delegate.WrappedNavigationDelegate

import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession

import org.mozilla.geckoview.GeckoView;

internal class GeckoViewInstance(context: Context, private val proxy: GeckoViewProxy) {
    companion object {
        private var runtime: GeckoRuntime? = null;

        fun getRuntime(context: Context): GeckoRuntime {
            if(runtime == null) {
                runtime = GeckoRuntime.create(context)
            }

            return runtime!!
        }
    }

    private val TAG: String = GeckoViewInstance::class.java.name

    private val viewContext: Context

    private val view: GeckoView
    private val sessions: MutableMap<Int, GeckoSession> = HashMap();

    fun getView(): View {
        return view
    }

    init {
        Log.d(TAG, "Initializing GeckoView")

        view = GeckoView(context)
        viewContext = context
    }

    fun init() {
    }

    fun createTab(tabId: Int) {
        if(sessions.containsKey(tabId)) {
            throw InternalError("Internal tab id reused")
        }

        val session = GeckoSession();
        sessions[tabId] = session;

        session.promptDelegate = FlutterPromptDelegate(proxy);
        session.navigationDelegate = WrappedNavigationDelegate()

        session.open(getRuntime(viewContext));
    }

    private fun getSessionByTabId(tabId: Int): GeckoSession {
        if(!sessions.containsKey(tabId)) {
            throw InternalError("Tab does not exist")
        }

        return sessions[tabId]!!
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
        if(navigation is WrappedNavigationDelegate) {
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
}