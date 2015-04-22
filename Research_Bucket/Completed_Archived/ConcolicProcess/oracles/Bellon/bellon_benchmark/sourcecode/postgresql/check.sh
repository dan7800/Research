#!/usr/local/bin/bash

DIR=`pwd`/src

find $DIR -name "*.c" -exec cc -Xc -w -xe -DPKGLIBDIR=\"/usr/local/pgsql/lib\" -DDLSUFFIX=\".so\" -I $DIR/include -I $DIR/backend {} \;

