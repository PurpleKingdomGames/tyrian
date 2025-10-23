#!/usr/bin/env bash

set -e

./mill clean
./mill __.compile
./mill -j2 __.test
./mill -j2 __.fastLinkJS
./mill __.publshLocal
