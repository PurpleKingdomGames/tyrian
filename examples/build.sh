#!/usr/bin/env bash

cd bootstrap
sbt clean test fastOptJS
cd ..

cd bundler
sbt clean test fastOptJS
cd ..

cd clock
sbt clean test fastOptJS
cd ..

cd counter
sbt clean test fastOptJS
cd ..

cd field
sbt clean test fastOptJS
cd ..

cd http
sbt clean test fastOptJS
cd ..

cd mario
sbt clean test fastOptJS
cd ..

cd subcomponents
sbt clean test fastOptJS
cd ..
