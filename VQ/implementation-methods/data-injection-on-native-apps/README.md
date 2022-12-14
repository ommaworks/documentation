# Native Data Injection with Post Message API
The personalization of a VQ content boils down to evaluating the decision points and text values on run-time. Every `variable` that is referenced on a scene template can be replaced by its actual value.


## Android Native Implementation
There are two main things to run VQ content inside a native WebView:
- Data flow from javascript side to native side
- Function calls from native side to javascript side.

Here are the steps:
Configure your WebView to allow javascript execution
```java
webView.getSettings().setJavaScriptEnabled(true);
```
To get events initialized from VQ content, we need a javascript bridging interface. It creates an object named `MessagingBridge` under `window` scope.
```java
class MessagingBridge {
    @JavascriptInterface
    public void send(String data){ ... }
}

webView.addJavascriptInterface(new MessagingBridge(), "MessagingBridge");
```
Proxy events using that messaging bridge
```java
void initMessagingInterface() {
    String eventListenerScript = """
       window.addEventListener('message', function (e) {
            const data = JSON.parse(e.data);
            if (data.event === 'ready-for-injection')
                window.MessagingBridge.send(JSON.stringify(data));
       });
    """;    
    webView.evaluateJavascript(eventListenerScript, null);
}
```
Execute it, just after the page started loading
```java
webView.setWebViewClient(new WebViewClient() {
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon){
        super.onPageStarted(view, url, favicon);
        initMessagingInterface();
    }
});
```
Now, we are ready to get javascript messages from the native side.

The second part is sending data from native side to javascript side.  VQ has an interface named `postMessage` to communicate. A simple call is like that:
```javascript
window.postMessage(JSON.stringify({ <your-data> }), '*')
```

Simply we can use a method to execute is from native side.
```java
void postMessage(String data) {
    String postMessageScript = "window.postMessage('" + data + "', '*');";
    webView.evaluateJavascript(postMessageScript, null);
}
```

So, we got a communication line. An example handshake is like:
```java
class MessagingBridge {
    @JavascriptInterface
    public void send(String eventType, JSONObject payload){
        JSONObject jsonData = new JSONObject(data);
        String eventName = jsonData.optString("event", "");

        if (eventName.equals("ready-for-injection")) {
            postMessage(<your-data-here>);
        }
    }
}
```

There is an example project [here](./android-example).

## iOS/macOS Native Implementation with Swift

We'll be using `WKWebView` class to display a VQ content. Therefore the first thing to do is importing `WebKit` namespace.

```swift
import WebKit
```

Let's create an `WKWebView` instance:

```swift
let webConfiguration = WKWebViewConfiguration()
webConfiguration.allowsInlineMediaPlayback = true
webConfiguration.mediaTypesRequiringUserActionForPlayback = []

let webView = WKWebView(frame: self.view.frame, configuration: webConfiguration)
```

`allowsInlineMediaPlayback` and `mediaTypesRequiringUserActionForPlayback` configurations are required for VQ contents to work properly.

Create a communication bridge named `vqHandler` between native side and javascript side.

```swift
let contentController = webView.configuration.userContentController
contentController.add(self, name: "vqHandler")
```

Now, we'll inject a code that proxies `ready-for-injection` event from javascript side to native side:

```swift
let js = """
    window.addEventListener('message', function(e) {
        const data = JSON.parse(e.data);
        if (data.name === 'ready-for-injection') {
            window.webkit.messageHandlers.vqHandler.postMessage(event);
        }
    });
"""
let script = WKUserScript(source: js, injectionTime: .atDocumentStart, forMainFrameOnly: false)
contentController.addUserScript(script)
```

To recieve this event, your implentor class must implement `WKScriptMessageHandler` protocol.

```swift
class ViewController: UIViewController, WKScriptMessageHandler {
    // ...
}
```

Add the protocol method `userContentController(_:didReceive:)`. This method will be called when a message is sent from javascript side.

```swift
func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    if message.name == "vqHandler" {
        print("Recieved event", message.body)
    }
}
```

Let's create a method that sends data back to javascript side:

```swift
func postMessage(_ data: String) {
    let js = "window.postMessage('\(data)', '*');"
    self.webView!.evaluateJavaScript(js)
}
```

When we recieve `ready-for-injection` event, we want to respond back with related data:

```swift
func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
    if message.name == "vqHandler" {
        print("Recieved event", message.body)

        let body = message.body as! [String: Any]
        let eventName = body["event"] as! String

        if eventName == "ready-for-injection" {
            let response: [String : Any] = [
                "event": "inject",
                "payload": [
                    // the data you want to inject
                ]
            ]

            do {
                let data = try JSONSerialization.data(withJSONObject: response)
                let dataStr = String(data: data, encoding: String.Encoding.utf8)!
                self.postMessage(dataStr)
            } catch {
                print("Could not json-stringify response: \(error)")
            }
        }
    }
}
```

An example project with Swift can be found [here](./swift-example).
