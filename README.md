# busymachines-commons

Light-weight, modular, libraries for varying technology stacks, built _primarily_ on top of the [typelevel.scala](https://github.com/typelevel) ecosystem.

## Quickstart

Current version is `0.1.0-SNAPSHOT`.

*_Currently there is no CI that automatically publishes versions, so you'll have to clone the repo, and do a `+ publishLocal` from the `sbt` repl for each module(`core`). This will be fixed ASAP._*

These modules are are cross-compiled for Scala versions: `2.12.3` and `2.11.11`. We try our best to keep them up to date.

Modules:
* `"com.busymachines" %% "busymachines-commons-core" % "0.1.0-SNAPSHOT"` [README.md](/core)

## Library Structure

The idea behind these sets of libraries is to help jumpstart backend RESTful api application servers with varying technology stacks. That's why you will have to pick and choose the modules suited for your specific stack.

Basically, as long as modules reside in the same repository they will be versioned with the same number, and released at the same time to avoid confusion. The moment we realize that a module has to take a life of its own, it will be moved to a separate module and versioned independently.

* [core](/core) `0.1.0-SNAPSHOT`

### Current version
The latest version is `N/A`. Will keep you up to date.

## Developer's Guide

This section will have to be expanded more once there are more projects living here.

## Contributing

Currently, if you want to contribute use the `fork+pull request` model, and Busy Machines team members will have a look at your PR asap. Currently, the active maintainers of this library are:
* @lorandszakacs

### History

This used to be the resting place of the `busymachines-commons` library that we used internally for various projects, it reached version `0.6.5`, but it fell into disrepair. If you require that library, by any chance, then check-out the `zz_deprecated/release-0.6.5` branch, and good luck from there. That bit will never be maintained again. And what you find resembles what was only by accident.

People who have created this history of 935 commits are (listed in order of commits):
* @rditerwich
* @paulsabou
* @mateialexandru
* @lorandszakacs
* @cristiboariu
* @adrianbumbas
* @nfazekas
* @advorkovyy
* @hipjim
* @mihaiSimu
* @scalastuff

You can use github API to get a list of contributors from a public project. At the time of writing you can use:
```
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET https://api.github.com/repos/busymachines/busymachines-commons/contributors  > contributors.json
```
### Contributors

People who have contributed to the new version are:
* @lorandszakacs
