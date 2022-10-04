# Tyrian Examples

We have a number of examples.

If you type `sbt` on your command prompt you will be presented with a welcome message. Many of the examples can be built from the aliases presented in the  prompt.

Alternatively, to build most of them you can run `sbt counter/fastOptJS` to take one example.

You then need to run them by `cd`'ing into the relevant directory and running `yarn install` and then `yarn start` to launch a Parcel.js based dev server. If you navigate to the page shown in your terminal (typically [http://localhost:1234/](http://localhost:1234/)) you'll be able to see the running example.

## Other examples

There are some examples that do not follow the instructions above, and they are:

### Bundler example

The bundler example requires `sbt bundler/fastOptJS::webpack` to work.

You will then need to run a local http server from within the `examples/bundler` directory, and navigate to [http://localhost:8080/](http://localhost:8080/) (for example) in your browser.

If you need an http server, you can install and run one like this:

```sh
npm install -g http-server
http-server -c-1
```

### Mill example

The [Mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) example has it's own README to follow.

To run the program in a browser you will need to have yarn (or npm) installed.

First, on your terminal `cd` into the relevant directory (e.g. `cd mill`), then on first run:

```sh
yarn install
```

Then

```sh
mill counter.buildSite
```

and then...

```sh
yarn start
```

Then navigate to [http://localhost:1234/](http://localhost:1234/)

### Server / SSR Examples

To run the server examples, `cd` into the server-examples folder, and run:

```sh
sbt start
```

Then navigate to [http://localhost:8080/](http://localhost:8080/) in your browser.

### WebSockets

The web sockets example is built like a normal example, however, before you open the page in your browser, you need to start the socket server for it to talk to.

In a new terminal tab, go to the `examples/websocket/server` folder, then run:

```sh
yarn install
node websocketserver.js
```

Then navigate to [http://localhost:1234/](http://localhost:1234/) as normal.
