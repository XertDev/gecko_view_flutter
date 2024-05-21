function handleError(error) {
    console.log(`Error: ${error}`);
}

const tabIdRequest = browser.runtime.sendMessage({
    type: "tabId"
})

function handleTabId(message) {
    browser.runtime.sendNativeMessage("browser", {type: "tabId", value: message["tabId"]});
    console.log("tabId setup procedure finished!");
}

console.log("Triggering tabId setup procedure...")
tabIdRequest.then(handleTabId, handleError);



