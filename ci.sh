#!/usr/bin/env bash

set -e

./mill clean
./mill -j1 __.compile
./mill -j1 __.test
./mill -j1 __.checkFormat
./mill -j1 __.fix --check
./mill -j1 __.fastLinkJS
./mill -j1 __.publshLocal
