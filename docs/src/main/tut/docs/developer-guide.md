---
layout: docs
title: Developer's Guide
---
# Developer's Guide

If you are responsible for publishing this library then you _have_ to follow instructions listed in [Publishing Artifacts](publishing-artifacts.html), otherwise you can simply ignore that folder.

Have throw-away code? Just create a package `playground` anywhere, it is already in `.gitignore` specifically for this purpose. Just make sure it doesn't end up in production/test code :)

## build structure
The build is fairly straightforward. The root folder contains a phantom build from which all modules are configured, and an essentially empty project "busymachines-commons" that is never published, but one that is extremely useful for importing into your IDEs. Most top level folders `X` correspond to the specific `busymachines-commons-X` library. All dependencies are spelled out in the `./build.sbt` file

## scalafmt

This codebase is formatted using [scalafmt](http://scalameta.org/scalafmt/). A simple `sbt scalafmt` formats your entire code. There are plenty of ways of using it with your favorite editor, as well.

There's an [IntelliJ plugin](https://plugins.jetbrains.com/plugin/8236-scalafmt) that you can/should use.