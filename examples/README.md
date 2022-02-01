# Warning!!

Normally these examples are bound to the last published version, but Tyrian is undergoing an overhaul, and these examples are being used for testing.

As such, they are temporarily pointing at the in-development version of Tyrian.

For the examples looking at the currently published versions, please refer to the release commit:

[https://github.com/PurpleKingdomGames/tyrian/tree/5f4f65246e3d9db7cc490e4dc17d097c13c78fd8/examples](https://github.com/PurpleKingdomGames/tyrian/tree/5f4f65246e3d9db7cc490e4dc17d097c13c78fd8/examples)

## Tyrian Examples

There are a number of examples. If you type `sbt` on your command prompt, maybe of the examples can be built from the alias presented in the welcome prompt.

Alternatively, to build most of them, you can just run, for example `sbt counter/fastOptJS`.

You then need to run them by `cd`'ing into the relevant directory and running `yarn start`.

### Other example

There are exceptions to the above.

#### Bundler example

The bundler example requires `sbt bundler/fastOptJS::webpack` to work.

You will then need to run a local http server from within the `examples/bundler` directory, and navigate to [http://localhost:8080/](http://localhost:8080/) for example in your browser.

If you need an http server, you can install and run one like this:

```sh
npm install -g http-server
http-server -c-1
```

#### Mill example

The [Mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) example has it's own README to follow.

To run the programs in a browser you will need to have yarn (or npm) installed.

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

#### Server / SSR Examples

To run the server examples, `cd` into the server-examples folder, and run:

```sh
sbt start
```

Then navigate to [http://localhost:8080/](http://localhost:8080/) in your browser.

#### WebSockets

The web sockets example is built like a normal example, however, before you open the page in your browser, you need to start the socket server for it to talk to.

In a new terminal tab, go to the `examples/websocket/server` folder, then run:

```sh
node websocketserver.js
```

Then navigate to [http://localhost:1234/](http://localhost:1234/) as normal.
