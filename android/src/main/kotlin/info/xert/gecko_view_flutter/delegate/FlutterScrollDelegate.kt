package info.xert.gecko_view_flutter.delegate

import info.xert.gecko_view_flutter.common.Offset
import org.mozilla.geckoview.GeckoSession

class FlutterScrollDelegate: GeckoSession.ScrollDelegate {
    var scrollOffset: Offset = Offset(0, 0)
        private set


    override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
        scrollOffset = Offset(scrollX, scrollY)

        super.onScrollChanged(session, scrollX, scrollY)
    }
}