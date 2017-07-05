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

# Build classpath.
_LIB="$BASEDIR/lib"
_CLASSPATH=$BASEDIR/etc
while read -d $'\0' file
do
    _CLASSPATH=$_CLASSPATH:$file;
done < <(find "$_LIB" -name "*.jar" -print0)

# Execute the program
$_JAVA $JAVA_OPTS -classpath "$_CLASSPATH" eu.fbk.dh.tint.runner.TintServer "$@"
