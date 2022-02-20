[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Ftyrian%2Ftags)](https://github.com/PurpleKingdomGames/tyrian/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.com/channels/716435281208672356)
[![CI](https://github.com/PurpleKingdomGames/tyrian/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/tyrian/actions/workflows/ci.yml)

# Tyrian

An Elm-inspired Scala UI library for Scala 3.

The main documentation site, complete with installation instructions, is available here:
[tyrian.indigoengine.io/](tyrian.indigoengine.io/)

## Local build instructions

Tyrian is fairly straight forward to build locally, run the following commands from your terminal in the project root:

```sh
npm install
sbt clean update compile test +publishLocal
```

There is also a `build.sh` script in the root folder, but that also builds the docs and example projects (based on the currently released version).
