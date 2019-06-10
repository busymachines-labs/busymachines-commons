#!/usr/bin/env bash

CACHE=~/.ivy2/local/com.busymachines/busymachines-commons-*
LOCAL=~/.ivy2/cache/com.busymachines/busymachines-commons-*

echo "purging local ivy cache of com.busymachines artifacts"
echo "@ $LOCAL"
ls -l $LOCAL
echo "----------------"
echo "@ $CACHE"
ls -l $CACHE
echo "----------------"
rm -v -rf $LOCAL
rm -v -rf $CACHE
