//
//  ViewController.swift
//  vq-swift-postmessage-poc
//
//  Created by Deniz Gurkaynak on 12.04.2021.
//

import UIKit
import WebKit

class ViewController: UIViewController, WKScriptMessageHandler {

    var webView: WKWebView?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        let webConfiguration = WKWebViewConfiguration()
        webConfiguration.allowsInlineMediaPlayback = true
        webConfiguration.mediaTypesRequiringUserActionForPlayback = []
        
        self.webView = WKWebView(frame: self.view.frame, configuration: webConfiguration)
        self.view.addSubview(self.webView!)
        
        let contentController = self.webView!.configuration.userContentController
        contentController.add(self, name: "vqHandler")
        
        let js = """
            window.addEventListener('message', function(e) {
                const event = JSON.parse(e.data);
                if (event.name === 'VQEvent') {
                    window.webkit.messageHandlers.vqHandler.postMessage(event);
                }
            });
        """
        let script = WKUserScript(source: js, injectionTime: .atDocumentStart, forMainFrameOnly: false)
        contentController.addUserScript(script)
        
        let url = URL(string: "https://play.omma.io/f8dda4724622a7821f14b3627b45a7ebf0d213c22130e54496e1d16dd13fefba/index.html")!
        let request = URLRequest(url: url)
        self.webView!.load(request)
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name == "vqHandler" {
            print("message got", message.body)
            
            let body = message.body as! [String: Any]
            let payload = body["payload"] as! [String: Any]
            let eventName = payload["eventName"] as! String
            
            if eventName == "ready_for_data_injection" {
                let response: [String : Any] = [
                    "eventName": "setVariables",
                    "variables": [
                        "payload": [
                            "name1": "George Bluth",
                            "image1": "https://reqres.in/img/faces/1-image.jpg",
                            "name2": "Janet Weaver",
                            "image2": "https://reqres.in/img/faces/2-image.jpg",
                        ]
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
        let js = "window.postMessage('\(data)');"
        self.webView!.evaluateJavaScript(js)
    }
}

