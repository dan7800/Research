#!/usr/local/bin/bash

DIR=`pwd`/src

for SUBDIRS in src/*
do
    find $SUBDIRS -name "*.c" -exec cc -w -Xc -xe -I $SUBDIRS -I $DIR/common {} \;
done

