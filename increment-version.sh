#!/bin/bash

#check to see if the minor version should be incremented
if [[ $1 == "minor" ]]; then
    mvn build-helper:parse-version versions:set -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.nextMinorVersion}.0' versions:commit -DgenerateBackupPoms=false
#check to see if the patch version should be incremented
elif [[ $1 == "patch" ]]; then
    mvn build-helper:parse-version versions:set -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}' versions:commit -DgenerateBackupPoms=false
else
    echo "usage: $programname [minor|patch]"
    exit 1;
fi
