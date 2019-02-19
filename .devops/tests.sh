#!/usr/bin/env bash

mkdir tmp

touch tmp/io.auklet.core.TestUtil.testDeleteQuietly tmp/AukletAuth

gradle test jacocoTestReport

rm -Rf tmp