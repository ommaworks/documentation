package com.ommasign.vq_messaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    WebView webView;
    String contentUrl = "https://play.omma.io/c/C5wzQ9/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new MessagingBridge(), "MessagingBridge");

        webView.loadUrl(contentUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                initTestMark();
                initMessagingInterface();
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // Network level errors can be handled here. SSL failure, timeout etc.
                Toast.makeText(MainActivity.this, "Cannot load page: " + error.toString(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                // Http errors can be handled here. 4xx, 5xx etc.
                int statusCode = errorResponse.getStatusCode();
                if (request.getUrl().toString().equals(contentUrl)) {
                    Toast.makeText(MainActivity.this, "Cannot load page: " + request.getUrl().toString() + " \r\n Status Code:" + statusCode, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    void initTestMark() {
        String prependText =
            "var g = document.createElement('div');" +
            "g.innerHTML = '<span>Testing...</span>';" +
            "g.style.zIndex=1111; g.style.color='#ffffff'; g.style.backgroundColor='#2B3A55';" +
            "g.style.position='absolute'; g.style.left=10; g.style.top=10;" +
            "document.body.prepend(g);" +
            "console.log('message: Test mark is ready');";
        webView.evaluateJavascript(prependText, null);
    }

    void initMessagingInterface() {
        String eventListenerScript =
                "window.addEventListener('message', function (e) {" +
                        "    const data = JSON.parse(e.data);" +
                        "    if (data.event === 'ready-for-injection') {" +
                        "        console.log('sendToNativeFromJs', data);" +
                        "        window.MessagingBridge.send(JSON.stringify(data));" +
                        "    }" +
                        "  });";
        webView.evaluateJavascript(eventListenerScript, null);
    }

    void postMessage(String data) {
        // We need to escape single quotes, because we're passing it inside single quotes
        String escapedData = data.replace("'", "\\'");

        String postMessageScript = "window.postMessage('" + escapedData + "', '*');";
        webView.evaluateJavascript(postMessageScript, null);
    }

    class MessagingBridge {
        @JavascriptInterface
        public void send(String data) {
            try {
                JSONObject jsonData = new JSONObject(data);
                String eventName = jsonData.optString("event", "");

                if (eventName.equals("ready-for-injection")) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                JSONObject payload = new JSONObject();
                                payload.put("segment", "A");
                                payload.put("name", "Shaq O'Neal");
                                payload.put("accountBalance", 1500);
                                payload.put("creditCardLimit", 2500);

                                JSONObject response = new JSONObject();
                                response.put("event", "inject");
                                response.put("payload", payload);

                                postMessage(response.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}