#!/usr/bin/env bash

cd bootstrap
sbt test fastOptJS
cd ..

cd clock
sbt test fastOptJS
cd ..

cd counter
sbt test fastOptJS
cd ..

cd field
sbt test fastOptJS
cd ..

cd http
sbt test fastOptJS
cd ..

cd mario
sbt test fastOptJS
cd ..

cd subcomponents
sbt test fastOptJS
cd ..
