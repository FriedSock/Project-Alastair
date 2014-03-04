#!/bin/bash
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
java -cp $SCRIPT_DIR/bin:$SCRIPT_DIR/jcommander.jar:$SCRIPT_DIR/antlr-3.4-complete.jar srt.test.StupidVisitorTest

