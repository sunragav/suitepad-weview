# suitepad-weview

This is a simple application that has only a webview.
It binds to the android-service (a messenger service via a service connection object), named ProxyServer,  from another app and waits for it start the http server in a secured way using the localhost.bks keystore.
The Proxy service notifies that it has started the http server, in a secured mode, with a message passed via the messenger.
This application listens to that messsage via its IncomingHandler and loads the url "https://someremoteurl.com/sample.html".
The webview's client is actually a custom OkHttp client(a subclass of WebViewClient) which proxies that request to "https://localhost:8091/sample.html".
This is done by intercepting the call in the shouldInterceptRequest mehtod of the WebViewClient class. So every call that refers to the
domain someremoteurl.com that the webview requests will be intercepted and changed to localhost by the okhttpclient. Also it uses a custom
HostNameVerifier to verify the localhost as a domain.
Because the connection is a secured TLS based connection, and our local server is using self-signed certificate, we need to add a custom
CA to the trust anchor for the connection to succeed. This configuration is done via the network-security-config.xml file which points to the localhost-certificate.crt file which is in an android understandable DER format. This certificate is used as a trust anchor to approve the connection to the localhost.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />
    <domain-config>
        <domain includeSubdomains="true">localhost</domain>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="@raw/localhost_certificate"/>
        </trust-anchors>
    </domain-config>
</network-security-config>
```

The server returns the result with an html page which has a javascript and an AJAX Query in it, which again an https url.
So webview makes another request to load the json to the server. 
The server serves that as well, after which the webview displays the content.

<img src="https://media.giphy.com/media/jrobNNOqQQpvy7cR8Q/giphy.gif" width="400">


This app requires the other 2 apps ProxyServer and the FileProvider to be installed, before it can run.

<img src="https://i.imgur.com/J3ssdc5.png" width="400">

This app defines a custom permission which the other apps uses. So it should be installed first. Otherwise the other apps cannot find the permission that they use.
All apps run in their own separate process.

Note that the android service is a Bound service.
Once the android service has started the http server, the communication happens directly between the http server and the webview (the  with oKhttp client interception) in the app using secured socket connection.
The service gets unbound and stopped once the activity finishes, which kills the http server as well.
To verify that the app uses a secured connection and that it is being proxied to the https://localhost:8091/sample.html, try to hit the localhost from the chrome browser while the app is running (Note that because it is a bound service that runs the http server behind the scenes, the http server stops as soon as the webview app is killed).
The chrome will warn this site as not trusted as we have used a custom seslf-signed certificate.
When we see the certificate we that the localhost http server indeed returning our self signed certificate.

<img src="https://i.imgur.com/mHgNHNg.jpg" width="400">

When we ignore and visit the site we will see the sample.html loaded with the html table headers but not the json. Because the sample.html returned will refer to the https://someremoteurl.com/sample.json which can not be routed to localhost by the chrome.

<img src="https://i.imgur.com/GNDpNBi.jpg" width="400">


