#!/usr/bin/env bash

set -e

sbt buildExamples testAll

cd mill
mill clean
mill counter.buildSite
cd ..

cd server-examples
sbt buildAll
cd ..
