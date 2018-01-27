#!/usr/bin/env bash

#
# Attempts to open the coverage report for the module specified as the only CL parameter
#

if (( $# == 0 ));
then
  echo ""
  echo "ERROR executing coverage.sh"
  echo "No command line arguments passed"
  echo "Please specify one valid module name to open report of."
  echo "... exiting"
  echo ""
  exit 1
else
  module="$1"
  open ./$module/target/scala-2.12/scoverage-report/index.html
fi

