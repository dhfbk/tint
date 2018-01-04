#!/bin/bash

# resolve program name in case it is a symbolic link
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
BINDIR=`dirname "$PRG"`
BASEDIR=`cd "$BINDIR" >/dev/null; pwd`


# Retrieve the path of the java executable.
_JAVA="java"
if [ -n "$JAVA_HOME"  ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
                _JAVA="$JAVA_HOME/jre/sh/java"
    elif [ -x "$JAVA_HOME/bin/java" ] ; then
                _JAVA="$JAVA_HOME/bin/java"
        fi
fi

$_JAVA $JAVA_OPTS -classpath "$BASEDIR"'/lib/*' eu.fbk.dh.tint.runner.TintRunner "$@"
