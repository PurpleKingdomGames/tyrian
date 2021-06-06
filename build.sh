#!/usr/bin/env bash

# Run from root.

sbt clean update compile test +tyrian/publishLocal
