#!/bin/bash
javac -nowarn -classpath .:../rt.jar:j2ee.jar:log4j-core.jar:jakarta-oro-2.0.5.jar `find -name "*.java"`

