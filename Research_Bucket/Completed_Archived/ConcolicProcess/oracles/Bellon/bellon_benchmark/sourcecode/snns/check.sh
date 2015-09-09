#!/usr/local/bin/bash

DIR=`pwd`/src

find $DIR/kernel -name "*.c" -exec cc -w -Xc -xe -I $DIR -I $DIR/kernel/sources {} \;

find $DIR/tools -name "*.c" -exec cc -w -Xc -xe -I $DIR -I $DIR/tools/sources -I $DIR/kernel/sources {} \;

find $DIR/xgui -name "*.c" -exec cc -w -Xc -xe -I $DIR -I $DIR/xgui/sources -I $DIR/kernel/sources -I $DIR/xgui/iconsXgui {} \;

