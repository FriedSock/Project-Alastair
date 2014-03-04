#!/bin/bash
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
java -cp $SCRIPT_DIR/bin srtester.Main "$@"

