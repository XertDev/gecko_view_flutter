package info.xert.gecko_view_flutter.webextension

class TabDataInitializerExtension: Extension() {
    override val TAG: String
        get() = TabDataInitializerExtension::class.java.name
    override val extensionID: String
        get() = "tab-data-initializer@xert.info"
    override val extensionPath: String
        get() = "assets/webextension/tab_data_initializer/"
}