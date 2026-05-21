#!/usr/bin/env sh
#
# OMNIBUS Docker Android — Gradle Wrapper (Auto-Download)
# Se gradle-wrapper.jar não existe, baixa automaticamente do Gradle CDN
#

# Attempt to set APP_HOME
APP_HOME="$( cd "$( dirname "$0" )" && pwd )"

# Resolve links: $0 may be a link
PRG="$0"
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname "$PRG"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar"

# Auto-download wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "[OMNIBUS] gradle-wrapper.jar não encontrado. Baixando..."
    if command -v curl >/dev/null 2>&1; then
        curl -L "$WRAPPER_URL" -o "$WRAPPER_JAR" --create-dirs
    elif command -v wget >/dev/null 2>&1; then
        wget "$WRAPPER_URL" -O "$WRAPPER_JAR" --force-directories --no-check-certificate
    else
        echo "ERRO: curl ou wget necessários para baixar gradle-wrapper.jar"
        echo "Baixe manualmente de: https://services.gradle.org/distributions/gradle-8.7-bin.zip"
        exit 1
    fi
    echo "[OMNIBUS] gradle-wrapper.jar baixado."
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Collect all arguments for the java command
set --         "-Dorg.gradle.appname=$APP_BASE_NAME"         -classpath "$WRAPPER_JAR"         "org.gradle.wrapper.GradleWrapperMain"         "$@"

exec "$JAVACMD" "$@"
