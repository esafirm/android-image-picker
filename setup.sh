#!/bin/bash
rm settings.gradle
echo "include ':imagepicker'" > settings.gradle
echo "include ':rximagepicker'" >> settings.gradle

cat settings.gradle

cd $ANDROID_HOME
mkdir -p licenses

cat << EOF >> licenses/android-sdk-license
8933bad161af4178b1185d1a37fbf41ea5269c55
d56f5187479451eabf01fb78af6dfcb131a6481e
24333f8a63b6825ea9c5514f83c2829b004d1fee
EOF