# suitepad-weview

This is a simple application that has only a webview.
It starts the android-service , named ProxyServer, from another app and waits for it start the http server.
The Proxy service notifies that it has started the http server by sending a broadcast.
This application listens to that broadcast and loads the url "http://localhost:8091/sample.html
The server returns the result with an html page which has a javascript and an AJAX Query in it.
So the it makes another request to load the json to the server. 
The server serves that as well, after which the webview displays the content.

<img src="https://media.giphy.com/media/jrobNNOqQQpvy7cR8Q/giphy.gif" width="400">
Note that the android service is started as a forground service, so that it can launch other activities.
Once the android service has started the http server, the communication happens directlly between the http server and the webview in the app
using socket connection.
The service gets killed, which kills the http server also once the activity finishes.

This app requires the other 2 apps ProxyServer and the FileProvider to be installed, before it can run.

<img src="https://i.imgur.com/J3ssdc5.png" width="400">

