package com.example.vqmessaging;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MessagingBridge(), "MessagingBridge");

        webView.loadUrl("https://play.omma.io/f8dda4724622a7821f14b3627b45a7ebf0d213c22130e54496e1d16dd13fefba/index.html");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                initTestMark();
                initMessagingInterface();
            }
        });

    }

    void initTestMark () {
        String prependText =
                "var g = document.createElement('div');" +
                "g.innerHTML = '<h1>Testing...</h1>';" +
                "g.style.zIndex=1111; g.style.color='#ffffff';" +
                "document.body.prepend(g);" +
                "console.log('message: Test mark is ready');";
        webView.evaluateJavascript(prependText, null);
    }

    void initMessagingInterface() {
        String eventListenerScript =
                "window.addEventListener('message', function (windowEvent) {" +
                "    const stringifiedData = windowEvent.data;" +
                "    const event = JSON.parse(stringifiedData);" +
                "    if (event.name === 'VQEvent') {" +
                "        const eventName = event.payload.eventName;" +
                "        const payload = event.payload.payload;" +
                "        console.log('sendToNativeFromJs', eventName, payload);" +
                "        window.MessagingBridge.send(eventName, payload);" +
                "    }" +
                "  });";
        webView.evaluateJavascript(eventListenerScript, null);
    }

    void postMessage(String context, JSONObject data) {
        /** window.postMessage(JSON.stringify({ ... }), '*') */
        String postMessageScript =
                "window.postMessage('"+ data.toString() + "', '" + context + "');";
        webView.evaluateJavascript(postMessageScript, null);
    }

    class MessagingBridge {
        @JavascriptInterface
        public void send(String eventType, JSONObject payload){
            if (eventType.equals("ready_for_data_injection")) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            /**
                             * window.postMessage(JSON.stringify({
                             *    eventName: 'setVariables',
                             *    variables: {
                             *        error: null,
                             *        payload: {
                             *            name1: 'Janet'
                             *            ...
                             *        }
                             *    }
                             * }), '*');"
                             * */
                            JSONObject jsonData = new JSONObject();
                            JSONObject variablesJson = new JSONObject();
                            JSONObject payloadJson = new JSONObject();
                            jsonData.put("eventName", "setVariables");
                            jsonData.put("variables", variablesJson);
                            variablesJson.put("error", null);
                            variablesJson.put("payload", payloadJson);
                            payloadJson.put("name1", "George Bluth");
                            payloadJson.put("image1", "https://reqres.in/img/faces/1-image.jpg");
                            payloadJson.put("name2", "Janet Weaver");
                            payloadJson.put("image2", "https://reqres.in/img/faces/2-image.jpg");
                            postMessage("*", jsonData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}