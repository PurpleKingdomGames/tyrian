#!/bin/bash

set -e

WEBSITE_DIR=$(pwd)
PROJECT_DIR=$WEBSITE_DIR/..

# -----
# generate / check docs
cd $PROJECT_DIR
sbt gendocs
cd $WEBSITE_DIR

# -----
# build the site
hugo --cleanDestinationDir

# -----
# Publish
sbt clean makeSite ghpagesPushSite
