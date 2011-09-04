#!/bin/bash
#
# deploy.sh
#
# A convenience script to install the groundhog apk
#

DEVICEID=$1

if [ -z $DEVICEID ] ; then
    echo "ERROR: Must provide device id"
    echo "Usage: $0 <deviceid>"
    echo ""
    adb devices
    exit 1
fi

# Rebuild application
mvn clean
mvn package

echo ""
echo "Looking for installed groundhog package ..."
adb -s $DEVICEID shell pm list packages | grep com.andyspohn.android.groundhog
if [ $? == 0 ] ; then
  echo "Uninstalling existing groundhog ..."
  adb -s $DEVICEID uninstall com.andyspohn.android.groundhog
  adb -s $DEVICEID shell rm -r /sdcard/groundhog
fi

echo ""
echo "Installing new groundhog ..."
APKFILE=`ls -1 groundhog-app/target/groundhog-*.apk | head -1`
adb -s $DEVICEID install $APKFILE

echo ""
echo "Starting groundhog ..."
adb -s $DEVICEID shell am start -a android.intent.action.MAIN \
-c android.intent.category.LAUNCHER \
-n com.andyspohn.android.groundhog/.Groundhog
