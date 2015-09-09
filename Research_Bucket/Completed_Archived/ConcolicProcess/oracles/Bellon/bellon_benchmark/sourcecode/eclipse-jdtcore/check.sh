#!/bin/bash
javac -nowarn -classpath .:../rt.jar:ant.jar:jakarta-ant-1.4.1-optional.jar:resources.jar:runtime.jar:xerces.jar `find -name "*.java"`

