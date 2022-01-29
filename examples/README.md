# Warning!!

Normally these examples are bound to the last published version, but Tyrian is undergoing an overhaul, and these examples are being used for testing.

As such, they are temporarily pointing at the in-development version of Tyrian.

For the examples looking at the currently published versions, please refer to the release commit:

[https://github.com/PurpleKingdomGames/tyrian/tree/5f4f65246e3d9db7cc490e4dc17d097c13c78fd8/examples](https://github.com/PurpleKingdomGames/tyrian/tree/5f4f65246e3d9db7cc490e4dc17d097c13c78fd8/examples)

# Tyrian Examples

Here are a number of examples.

To build most of them, you can just run, for example `sbt counter/fastOptJS`.

There are exceptions, the bundler example requires `sbt bundler/fastOptJS:webpack` to work.

The [Mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) example has it's own README to follow.

To run the programs in a browser you will need to have yarn (or npm) installed.

First, on your terminal `cd` into the relevant directory (e.g. `cd counter`), then on first run:

```sh
yarn install
```

and from then on

```sh
yarn start
```

Then navigate to [http://localhost:1234/](http://localhost:1234/)
