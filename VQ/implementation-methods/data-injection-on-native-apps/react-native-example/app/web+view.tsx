import {ThemedText} from '@/components/ThemedText';
import {ThemedView} from '@/components/ThemedView';
import {useRef} from 'react';
import {
  StatusBar,
  StyleSheet,
  View,
} from 'react-native';
import {WebView} from 'react-native-webview';


export default function HomeScreen() {
  const webViewRef = useRef<WebView>(null);

  const userData = {
    name: 'Jennifer',
    accountBalance: 995,
    creditCardLimit: 3500
  };

  const inject = () => {
    if (!webViewRef.current) {
      alert('WebView not ready to inject data');
      return;
    }

    webViewRef.current.injectJavaScript(`
      window.postMessage(JSON.stringify({
        event: 'inject', 
        payload: ${JSON.stringify(userData)},
      }), '*');

      true;
    `);
  };

  return (
    <ThemedView
      style={[
        styles.container,
        {flexDirection: 'column'},
      ]}>
      <ThemedView >
        <ThemedText type="title" style={[styles.titleContainer]}>VQ - Client Isolated Example</ThemedText>
      </ThemedView>
      <View style={{flex: 1, backgroundColor: 'darkorange'}} >
        <WebView
          ref={webViewRef} 
          webviewDebuggingEnabled
          javaScriptEnabled
          domStorageEnabled
          startInLoadingState
          source={{uri: `https://play.omma.io/c/_eq0DD/index.html?ts=${Date.now()}`}}
          injectedJavaScriptBeforeContentLoaded={`
            window.onerror = function(message, sourcefile, lineno, colno, error) {
              alert("Message: " + message + " - Source: " + sourcefile + " Line: " + lineno + ":" + colno);
              return true;
            };

            window.addEventListener('message', event => {
              const isVqMessage = (typeof event.data === 'string');
              if (!isVqMessage) return;

              const vqMessage = JSON.parse(event.data);
              if (vqMessage.event === 'ready-for-injection') {
                  window.ReactNativeWebView.postMessage('ready-for-injection');
              };
            });

            true;
          `}
          onMessage={(event) => {
            const nativeEventData = event.nativeEvent.data;
            
            // Trigger the injection of the data when the player is ready
            if (typeof nativeEventData === 'string' && nativeEventData === 'ready-for-injection') {
              inject();
            }
          }}
        />
      </View>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: StatusBar.currentHeight,
  },
  scrollView: {
    backgroundColor: 'pink',
  },
  titleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
});
