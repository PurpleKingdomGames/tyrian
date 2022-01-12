# Tyrian Examples

Here are a number of examples.

To build most of them, you can just run, for example `sbt counter/fastOptJS`.

There are exceptions, the bundler example requires `sbt bundler/fastOptJS:webpack` to work. And the Mill example has it's own README to follow.

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
