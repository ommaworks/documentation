//
//  ViewController.swift
//  vq-swift-postmessage-poc
//
//  Created by Deniz Gurkaynak on 12.04.2021.
//

import UIKit
import WebKit

class ViewController: UIViewController, WKScriptMessageHandler, WKNavigationDelegate {

    var webView: WKWebView?
    let contentURL = "https://play-test.omma.io/c/x3dASS/index.html"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.allowsInlineMediaPlayback = true
        webConfiguration.mediaTypesRequiringUserActionForPlayback = []
        
        self.webView = WKWebView(frame: self.view.frame, configuration: webConfiguration)
        self.webView?.navigationDelegate = self
        self.view.addSubview(self.webView!)
        
        let contentController = self.webView!.configuration.userContentController
        contentController.add(self, name: "vqHandler")
        
        let js = """
            window.addEventListener('message', function(e) {
                const data = JSON.parse(e.data);
                if (data.event === 'ready-for-injection') {
                    window.webkit.messageHandlers.vqHandler.postMessage(data);
                }
            });
        """
        let script = WKUserScript(source: js, injectionTime: .atDocumentStart, forMainFrameOnly: false)
        contentController.addUserScript(script)
        
        let url = URL(string: contentURL)!
        let request = URLRequest(url: url)
        self.webView!.load(request)
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "vqHandler" {
            print("message got", message.body)
            
            let body = message.body as! [String: Any]
            let eventName = body["event"] as! String
            
            if eventName == "ready-for-injection" {
                let response: [String : Any] = [
                    "event": "inject",
                    "payload": [
                        "name": "world",
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
    
    func postMessage(_ data: String) {
        let js = "window.postMessage('\(data)', '*');"
        self.webView!.evaluateJavaScript(js)
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationResponse: WKNavigationResponse, decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        if let response = navigationResponse.response as? HTTPURLResponse {
            if response.url != nil && response.url!.absoluteString == contentURL && response.statusCode >= 400 {
                print("Cannot open VQ Content. Unexpected response code: \(response.statusCode)")
            }
        }
        
        decisionHandler(.allow)
    }
}

