package info.xert.gecko_view_flutter.webextension

import org.json.JSONObject

class HostJSExecutionExtension: Extension() {
    override val TAG: String
        get() = TabDataInitializerExtension::class.java.name
    override val extensionID: String
        get() = "host-js-execution@xert.info"
    override val extensionPath: String
        get() = "assets/webextension/host_js_execution/"

    fun runAsync(script: String, tabId: Int) {
        if(port != null) {
            val message = JSONObject()
            message.put("action", "execute");
            message.put("tabId", tabId)
            message.put("content", script)

            port?.postMessage(message)
        } else {
            throw InternalError("Host JS Execution Extension not initialized")
        }
    }
}