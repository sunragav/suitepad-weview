# suitepad-weview

## Note: This app defines a custom permission which the other apps uses for exporting their components between each other in a secured way. So it should be installed first. Otherwise the other apps ([ProxyServer](https://github.com/sunragav/suitepad-proxyserver) and the [FileProvider](https://github.com/sunragav/suitepad-localcache)) cannot find the permission that they use.


This is a simple application that has only a webview.
It binds to the android-service (a messenger service via a service connection object), named [ProxyServer](https://github.com/sunragav/suitepad-proxyserver),  from another app and waits for it start the http server in a secured way using the localhost.bks keystore.
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

## Dependent apps
This app requires the other 2 apps [ProxyServer](https://github.com/sunragav/suitepad-proxyserver) and the [FileProvider](https://github.com/sunragav/suitepad-localcache) to be installed, before it can run. All apps run in their own separate process.

<img src="https://i.imgur.com/J3ssdc5.png" width="400">

But this app should be installed first. When started to run without installing them will show a screen saying that the other components have to be installed.
The app identifies the packages that are missing and informs user to install both or only the one that is missing as shown below:

## Both are missing
<img src="https://i.imgur.com/AMCBx7M.jpg" width="400">

## Only File Provider is missing

<img src="https://i.imgur.com/ygQumXc.jpg" width="400">

## Only Proxy Server is missing

<img src="https://i.imgur.com/xsvmdkc.jpg" width="400">

Note that the android service is a Bound service.
Once the android service has started the http server, the communication happens directly between the http server and the webview (ofcourse  with the interception of the okHttp client which proxies the request to localhost) in the app using secured socket connection.
The service gets unbound and stopped once the activity finishes, which kills the http server as well.
To verify that the app uses a secured connection and that it is being proxied to the https://localhost:8091/sample.html, try to hit the localhost from the chrome browser while the app is running (Note that because it is a bound service that runs the http server behind the scenes, the http server stops as soon as the webview app is killed).
The chrome will warn this site as not trusted as we have used a custom seslf-signed certificate.
When we see the certificate we that the localhost http server indeed returning our self signed certificate.

<img src="https://i.imgur.com/mHgNHNg.jpg" width="400">

When we ignore and visit the site we will see the sample.html loaded with the html table headers but not the json. Because the sample.html returned will refer to the https://someremoteurl.com/sample.json which can not be routed to localhost by the chrome.

<img src="https://i.imgur.com/GNDpNBi.jpg" width="400">

# NOTE: THE RELEASE SIGNING KEY HAS BEEN ADDED JUST FOR THE SAKE OF COMPLETION AND DEMONSTRATION. THIS SHOULD BE HIDDEN AND KEPT SECRET FROM OTHERS IN A SECURED PLACED AND ACCESSED VIA CI/CD process.
## Without shrinking and without progaurd rules, the original release apk size is 2.9 MB and the downlaod size is 2.6 MB

![without-shrinking](https://i.imgur.com/RIzVe2n.jpg)

## With shrinking but without progaurd rules, the release apk size is 1.4 MB and the download size is 1.1 MB

![with-shrinking-alone-without-progaurd](https://imgur.com/8XvpnHi.jpg)

## Including the progaurd rules the release apk, size is 1.2 MB and the downlaod size is 963.2 KB

![with-progaurd](https://i.imgur.com/imbOTak.jpg)

## Shrinking vs (Shrinking+Progaurd) comparison (206.7 KB saved)

![shrinking-vs-progaurd](https://i.imgur.com/oTnFfdQ.jpg)

## Original vs (Shrinking+Progaurd) comparison ( 1.7 MB saved)

![orginal-vs-optimized](https://i.imgur.com/W6ueXMp.jpg)

By the way, because I have used Android studio 3.5.3 for the development,  the shrinking+obfuscation using progaurd rules are directly done using the R8 compiler to output the dex. There is no intermediate optimized java byte code generated like it was previously when using D8 compiler.

![R8](https://i.imgur.com/gaW51ac.jpg)







