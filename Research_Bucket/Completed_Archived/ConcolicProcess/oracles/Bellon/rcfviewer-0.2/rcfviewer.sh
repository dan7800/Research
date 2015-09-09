#!/bin/bash

# Directory where the script (and the jar directory) is located
SCRIPT_DIR=$(dirname $0)

# Determines which directory contains the needed jar files
JAR_DIR=${SCRIPT_DIR}/jar

# Adds all jar files to the classpath
CP=\
${JAR_DIR}/rcfviewer.jar:\
${JAR_DIR}/swt.jar

# Options passed to the Java VM
OPTIONS=-Xmx1024m

# Mac users need another option to make cyclone work
OS=`uname`
if [ $OS == 'Darwin' ]; then
    OPTIONS="${OPTIONS} -XstartOnFirstThread"
fi

# Now start cyclone using the assembled classpath. 
java ${OPTIONS} -cp ${CP} de.uni_bremen.st.rcfviewer.Viewer $*
