# Password Generator Re-frame

React password generator using Re-frame components

## Overview

Generate various kinds of passwords with various constraints.
This is based on Eric Normand's PurelyFunctional.tv (LispCast)
Re-frame Components course.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## Deploying

This has Ruby Rake tasks for deploying to a server using FTP.

The sample environment would be:

```
FTP_SERVER="myserver.org"
FTP_USER="frimmel"
FTP_PASSWORD="geheim"
FTP_DEBUG="false"
```
Kill the Figwheel development server and just do ...
```shell
rake --trace
```
## License

Copyright © 2017 Aventine Solutions

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
