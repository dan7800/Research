#!/bin/bash

# Directory where the script (and the jar directory) is located
SCRIPT_DIR=$(dirname $0)

# Options passed to the Java VM
OPTIONS=-Xmx1024m

# Now start iclones.
java ${OPTIONS} -cp "${SCRIPT_DIR}/jar/iclones.jar" de.uni_bremen.st.iclones.Commander $@
