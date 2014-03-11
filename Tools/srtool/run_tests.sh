#!/bin/sh

ONLY_REPORT_FAILURES=0

# Very simple test cases (to start with):
# Once you have the bounded model checker working, you can remove
# this line, as these tests are included below.
#tester/tester_run.sh tests/loopfree/1_simple $ONLY_REPORT_FAILURES -mode bmc

# Loop-free tests (should work with all modes)

#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode bmc
#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode bmc -unsound
#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode verifier
#tester/tester_run.sh tests/verifier $ONLY_REPORT_FAILURES -mode verifier
#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode houdini
#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode invgen
# Uncomment if you implement comp
#tester/tester_run.sh tests/loopfree $ONLY_REPORT_FAILURES -mode comp

# BMC sound tests
#tester/tester_run.sh tests/bmc_sound $ONLY_REPORT_FAILURES -mode bmc

# BMC unsound tests
#tester/tester_run.sh tests/bmc_unsound $ONLY_REPORT_FAILURES -mode bmc -unsound

# Houdini tests
tester/tester_run.sh tests/houdini $ONLY_REPORT_FAILURES -mode houdini

# Invariant generation tests
#tester/tester_run.sh tests/invgen $ONLY_REPORT_FAILURES -mode invgen

# Competition mode tests
#tester/tester_run.sh tests/comp $ONLY_REPORT_FAILURES -mode comp

