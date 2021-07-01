# Data Injection on Iframe

Optional requirement:
- [Postman.js](https://github.com/signalive/postman.js)

#### Iframe Implementation

```html
<iframe src="" id="VQ" allow="fullscreen"></iframe>

<!-- We recommend including postman.js however native events can be followed too -->
<script src="postman.min.js"></script>
```


```js
const VQ = document.getElementById('VQ');
VQ.src = "https://play.omma.io/e6c796ee526cf50ab493f3b0783bf41e7ee00db14618dc79c544918ce0dbe4e5/index.html?ts=" + Date.now();
```

#### Data Injection
```js
const main = async destination => {
  const injectData = {
    'name': 'John',
    'surname': 'Doe',
    'segment': 'A' //choose 'A' or 'B' to alternate between flows
  }

  VQ.contentWindow.postMessage(JSON.stringify({

    eventName: 'useLocals',
    locals: {
      payload: injectData
    }
  }), '*')

}
```


#### Event listening with the Postman.js;


```js

const client = postman.createClient(VQ.contentWindow, '*', 0);

client.on('VQEvent', event => {
  console.log(event);
  if (event.eventName == 'ready_for_data_injection') {
    main();
  }

});
```
#### Event listening with native methods;

```js

window.addEventListener('message', event => {
  const data = JSON.parse(event.data);
  if (data.payload.eventName == 'ready_for_data_injection') {
    console.log('[HTML] ready_for_data_injection received');
    main();
  }
});

```

---

See the full example on [container.html](./container.html)