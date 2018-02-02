#!/usr/bin/env bash
#simple script for purging the target folders of all sub-modules
#use in case of sbt bugs, like the one where it would crash when switching
#from 0.13.x to 1.x, then back again

find . -type d -name target -exec ls '{}' \;
