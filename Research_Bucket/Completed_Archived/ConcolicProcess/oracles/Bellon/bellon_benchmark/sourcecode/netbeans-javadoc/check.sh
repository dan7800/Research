#!/bin/bash
javac -nowarn -classpath .:../rt.jar:tools.jar:netbeans-support.jar $(find -name "*.java")
