#!/usr/bin/env bash

# Run from root.

set -e

PROJECT_ROOT=$(pwd)

sbt clean update compile testAllUnit +publishLocal gendocs

cd examples
sbt buildExamples
cd $PROJECT_ROOT

cd examples/mill
mill counter.compile
mill counter.test
cd $PROJECT_ROOT

cd examples/server-examples
sbt buildAll
cd $PROJECT_ROOT
