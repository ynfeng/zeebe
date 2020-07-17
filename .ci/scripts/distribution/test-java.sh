#!/bin/sh -eux


export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -XX:MaxRAMFraction=$((LIMITS_CPU))"

mvn -o -B -T$LIMITS_CPU -s ${MAVEN_SETTINGS_XML} verify -pl atomix -Dtests=AtomixTest -P skip-unstable-ci -Dzeebe.it.skip -DtestMavenId=1
