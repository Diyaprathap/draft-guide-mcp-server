#!/bin/bash
while getopts t:d:b:j: flag; do
    case "${flag}" in
    t) DATE="${OPTARG}" ;;
    d) DRIVER="${OPTARG}" ;;
    b) BUILD="${OPTARG}" ;;
    j) JDK_LEVEL="${OPTARG}" ;;
    *) echo "Invalid option input" ;;
    esac
done

echo "Testing daily build image"

if [ "$JDK_LEVEL" == "11" ]; then
    echo "Test skipped because the guide does not support Java 11."
    exit 0
fi

sed -i "\#<artifactId>liberty-maven-plugin</artifactId>#a<configuration><install><runtimeUrl>https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/nightly/$DATE/$DRIVER</runtimeUrl></install></configuration>" pom.xml ../start/pom.xml
cat pom.xml ../start/pom.xml

../scripts/testAppFinish.sh
../scripts/testAppStart.sh
