#!/usr/bin/env bash

# Run from root.

set -e

PROJECT_ROOT=$(pwd)

sbt clean update compile test +publishLocal gendocs

cd examples
sbt buildExamples

cd server-examples
sbt buildAll

cd $PROJECT_ROOT
