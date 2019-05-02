#!/usr/bin/env sh

# resolve links - $0 may be a softlink
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

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set ARCADEDB_HOME if not already set
[ -f "$ARCADEDB_HOME"/bin/server.sh ] || ARCADEDB_HOME=`cd "$PRGDIR/.." ; pwd`

cd "$ARCADEDB_HOME/bin"


# Raspberry Pi check (Java VM does not run with -server argument on ARMv6)
if [ `uname -m` != "armv6l" ]; then
  JAVA_OPTS="$JAVA_OPTS -server "
fi

# Set JavaHome if it exists
if [ -f "${JAVA_HOME}/bin/java" ]; then
   JAVA=${JAVA_HOME}/bin/java
else
   JAVA=java
fi


if [ -z "$ARCADEDB_PID" ] ; then
    ARCADEDB_PID=$ARCADEDB_HOME/bin/arcadedb.pid
fi

if [ -f "$ARCADEDB_PID" ]; then
    echo "removing old pid file $ARCADEDB_PID"
    rm "$ARCADEDB_PID"
fi


# ARCADEDB memory options, default to 2GB of heap.

if [ -z "$ARCADEDB_OPTS_MEMORY" ] ; then
    ARCADEDB_OPTS_MEMORY="-Xms2G -Xmx2G"
fi

if [ -z "$JAVA_OPTS_SCRIPT" ] ; then
    JAVA_OPTS_SCRIPT="-Djna.nosys=true -XX:+HeapDumpOnOutOfMemoryError -Djava.awt.headless=true -Dfile.encoding=UTF8 -Drhino.opt.level=9"
fi

if [ -z "$ARCADEDB_JMX" ] ; then
    ARCADEDB_JMX="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.rmi.port=9998"
fi

echo $$ > $ARCADEDB_PID

exec "$JAVA" $JAVA_OPTS \
    $ARCADEDB_OPTS_MEMORY \
    $JAVA_OPTS_SCRIPT \
    $ARCADEDB_JMX \
    $ARCADEDB_SETTINGS \
    -cp "$ARCADEDB_HOME/lib/*" \
    $ARGS "$@" com.arcadedb.server.ArcadeDBServer $*
