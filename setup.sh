#!/bin/bash
rm settings.gradle
echo "include ':imagepicker'" > settings.gradle
echo "include ':rximagepicker'" >> settings.gradle

cat settings.gradle
