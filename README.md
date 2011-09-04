Groundhog Android Application
=============================

This application is a mash-up of sorts that brings together the following pieces with the goal of allowing
an Android device to publish and receive sensor collects.

* __[Jetty 7](http://www.eclipse.org/jetty/) - Application Server__
* __[I-Jetty](http://code.google.com/p/i-jetty/) - Android Port of Jetty__
* __[CometD](http://cometd.org/) - HTTP-based event routing bus__
* __[JSTUN](http://jstun.javawi.de/) - NAT Translation Utility__



__Building__

1. To just build the apk file use the standard _mvn package_ command from the top level groundhog directory
and look in the groundhog-app/target directory.

__Build and Install__

1. Plug in your device
1. Run the top level ./deploy.sh script supplying your device id.
    * The script will run the _adb devices_ command for you and exit if you don't supply your device id
1. The script will rebuild the app, uninstall from device (if present), install newly compiled app to device and start it running.


__Licenses__

The Groundhog application and all component parts listed above are licensed under [Apache License - Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

The groundhog-app
module of this application is a forked version of the ijetty-ui portion of the [I-Jetty](http://code.google.com/p/i-jetty/) project.
I wanted to simplify the install so specific war file(s) are installed by default and no settings or controls other than starting or stopping
the app were presented to the user. All of the package and file names were changed as well so this app should in no way interfere
with an install of i-jetty on the same device.


__Thank You__

To all the folks involved with these projects for making and publishing their apps as open source!
