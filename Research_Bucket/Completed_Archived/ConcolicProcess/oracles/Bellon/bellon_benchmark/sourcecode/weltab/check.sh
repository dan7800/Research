#!/usr/local/bin/bash

DIR=`pwd`/src

find $DIR -name "*.c" -exec cc -w -Xc -xe -I $DIR {} \;

