@ECHO OFF



REM Directory where the script (and the jar directory) is located

set SCRIPT_DIR=%~dp0%



REM Determines which directory contains the needed jar files

set JAR_DIR=%SCRIPT_DIR%\jar



REM Adds all jar files to the classpath

set CP=%JAR_DIR%\rcfviewer.jar;%JAR_DIR%\swt.jar



REM Now start cyclone using the assembled classpath

java -Xmx1024m -cp %CP% de.uni_bremen.st.rcfviewer.Viewer

