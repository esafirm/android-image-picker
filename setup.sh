#!/bin/bash
rm settings.gradle
echo "include ':imagepicker'" > settings.gradle

cat settings.gradle

yes | $ANDROID_HOME/tools/bin/sdkmanager "build-tools;28.0.3"