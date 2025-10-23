#!/usr/bin/env bash

set -e

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat
./mill __.fix --check
./mill -j2 __.fastLinkJS
./mill __.publshLocal
