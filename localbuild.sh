#!/usr/bin/env bash

set -e

sbt clean update compile testAllUnit +publishLocal
