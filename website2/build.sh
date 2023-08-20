#!/bin/bash

set -e

WEBSITE_DIR=$(pwd)
PROJECT_DIR=$WEBSITE_DIR/..

# -----
# generate / check docs
#  - Docs in ./tyrian-docs/target/mdoc
#  - Docs in ./target/unidocs/site-docs
cd $PROJECT_DIR
sbt gendocs
cd $WEBSITE_DIR

# -----
# build the site // $WEBSITE_DIR/target/docs/site
sbt clean laikaSite

# -----
# Publish
mkdir -p target/docs/site/api/
cp -R $PROJECT_DIR/target/unidocs/site-docs/api/. $WEBSITE_DIR/target/docs/site/api/
sbt makeSite ghpagesPushSite
