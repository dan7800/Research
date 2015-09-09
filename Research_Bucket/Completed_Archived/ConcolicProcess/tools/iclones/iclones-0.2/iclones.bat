@ECHO OFF

REM Directory where the script (and the jar directory) is located

set SCRIPT_DIR=%~dp0%

REM Determines which directory contains the needed jar files

set JAR_DIR=%SCRIPT_DIR%\jar

REM Now start iclones using the assembled classpath

java -Xmx2048m -jar %JAR_DIR%/iclones.jar %*

