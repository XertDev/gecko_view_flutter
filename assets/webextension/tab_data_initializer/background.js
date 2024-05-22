'use strict';

function handleContentMessage(request, sender, sendResponse) {
    sendResponse({tabId: sender.tab.id});
}
browser.runtime.onMessage.addListener(handleContentMessage);