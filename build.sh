#!/usr/bin/env bash

set -e

sbt clean update compile test +publishLocal
