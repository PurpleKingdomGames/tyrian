[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Ftyrian%2Ftags)](https://github.com/PurpleKingdomGames/tyrian/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.gg/b5CD47g)
[![CI](https://github.com/PurpleKingdomGames/tyrian/actions/workflows/ci.yml/badge.svg)](https://github.com/PurpleKingdomGames/tyrian/actions/workflows/ci.yml)

# Tyrian

An Elm-inspired Scala UI library for Scala 3.

The main documentation site, complete with installation instructions, is available here:
[tyrian.indigoengine.io/](https://tyrian.indigoengine.io/)

## Local build instructions

Tyrian is fairly straight forward to build locally, run the following commands from your terminal in the project root:

```sh
npm install
sbt clean update compile test +publishLocal
```

There is also a `build.sh` script in the root folder, but that also builds the docs and example projects (based on the currently released version).

### Nix dev-shells

If you would like to have the environment set up without installing any dependencies globally (think `node`, `yarn`, `java`, etc), you can give the [Nix](https://nixos.org/) development shells a try.

There are three different shells, which require you to enable [flakes](https://nixos.wiki/wiki/Flakes#Installing_flakes).

#### Main

The `main` dev shell combines both the `jvm` and `js` shells described below.

```console
$ nix develop .#main
```

Locally you can run `nix develop .#main`. However, if you would like to use the same shell in a different repository, you can run the following command instead.

```console
nix develop github:PurpleKingdomGames/tyrian#main
```

#### JVM

The default `jvm` dev shell ships with jdk, mill, sbt and coursier, ensuring all dependencies use the same JDK.

```console
$ java --version
The program 'java' is not in your PATH. It is provided by several packages.

$ sbt --version
The program 'sbt' is not in your PATH. It is provided by several packages.

$ nix develop .#jvm

$ java --version
openjdk 17.0.1 2021-10-19
OpenJDK Runtime Environment (build 17.0.1+12-nixos)
OpenJDK 64-Bit Server VM (build 17.0.1+12-nixos, mixed mode, sharing)

$ mill --version
Mill Build Tool version 0.10.0
Java version: 17.0.1, vendor: N/A, runtime: /nix/store/drg31yiw0619r981n0yyv7lnziiyxwww-openjdk-headless-17.0.1+12/lib/openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "Linux", version: 5.16.10, arch: amd64

$ sbt --version
sbt version in this project: 	1.6.2
sbt script version: 1.6.2

$ cs java-home
/nix/store/drg31yiw0619r981n0yyv7lnziiyxwww-openjdk-headless-17.0.1+12
```

Or remotely.

```console
nix develop github:PurpleKingdomGames/tyrian#jvm
```

#### JS

The `js` shell ships with `node` and `yarn` by default.

```console
$ yarn --version
The program 'yarn' is not in your PATH. It is provided by several packages.

$ npm --version
The program 'npm' is not in your PATH. It is provided by several packages.

$ nix develop .#js

$ node --version
v16.14.0

$ npm --version
8.3.1

$ yarn --version
1.22.17
```

Also accessible remotely via the following command.

```console
nix develop github:PurpleKingdomGames/tyrian#js
```

#### Can I use the old `nix-shell`?

Absolutely! The `shell.nix` file provides a flakes-compatible shell that works with `nix-shell`. It selects the `main` shell by default, but you can specify which one you want. E.g.

```console
$ nix-shell --argstr shell jvm
```
