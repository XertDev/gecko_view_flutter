package info.xert.gecko_view_flutter.delegate

import org.mozilla.geckoview.GeckoSession

class WrappedNavigationDelegate: GeckoSession.NavigationDelegate {
    var currentUrl: String? = null
        private set


    override fun onLocationChange(session: GeckoSession, url: String?, perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>, hasUserGestures: Boolean) {
        currentUrl = url
        super.onLocationChange(session, url, perms, hasUserGestures)
    }
}