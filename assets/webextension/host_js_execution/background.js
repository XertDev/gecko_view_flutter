'use strict';

console.log("Connecting to native port...")
const port = browser.runtime.connectNative("browser");
console.log("Native port connected!")


port.onMessage.addListener(message => {
    console.log(message)
    if (message["action"] == "execute") {
        browser.tabs.executeScript(message["tabId"], {
            code: message["content"],
        });
    }
});
