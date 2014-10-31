#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  SoapUI Bootstrap Script                                                 ##
##                                                                          ##
### ====================================================================== ###

### $Id$ ###

if [ "x$SORCER_HOME" = "x" ];
then
     echo "Error, SORCER_HOME not set"
     return
fi

. "$SORCER_HOME/bin/common-run"

DIRNAME=`dirname $0`

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
    Darwin*)
        darwin=true
        ;;
esac

# Setup SOAPUI_HOME
if [ "x$SOAPUI_HOME" = "x" ];
then
    # get the full path (without any relative bits)
    SOAPUI_HOME=`cd $DIRNAME/..; pwd`
fi
export SOAPUI_HOME

SOAPUI_CLASSPATH=$SOAPUI_HOME/bin/soapui-5.0.0.jar:$SOAPUI_HOME/lib/*

export SOAPUI_CLASSPATH

JAVA_OPTS="-Xms128m -Xmx1024m -Dsoapui.properties=soapui.properties -Dsoapui.home=$SOAPUI_HOME/bin -splash:soapui-splash.png"
JFXRTPATH=`java -cp $SOAPUI_CLASSPATH com.eviware.soapui.tools.JfxrtLocator`
SOAPUI_CLASSPATH=$JFXRTPATH:$SOAPUI_CLASSPATH

if $darwin
then
    JAVA_OPTS="$JAVA_OPTS -Dswing.crossplatformlaf=apple.laf.AquaLookAndFeel -Dapple.eawt.quitStrategy=CLOSE_ALL_WINDOWS"
fi

if [ $SOAPUI_HOME != "" ] 
then
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.libraries=$SOAPUI_HOME/bin/ext"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.listeners=$SOAPUI_HOME/bin/listeners"
    JAVA_OPTS="$JAVA_OPTS -Dsoapui.ext.actions=$SOAPUI_HOME/bin/actions"
	JAVA_OPTS="$JAVA_OPTS -Djava.library.path=$SOAPUI_HOME/bin"
	JAVA_OPTS="$JAVA_OPTS -Dwsi.dir=$SOAPUI_HOME/wsi-test-tools"
#uncomment to disable browser component
#   JAVA_OPTS="$JAVA_OPTS -Dsoapui.browser.disabled=true"
fi

export JAVA_OPTS

# For Cygwin, switch paths to Windows format before running java
if [ $cygwin = "true" ] 
then
    SOAPUI_HOME=`cygpath --path --dos "$SOAPUI_HOME"`
    SOAPUI_CLASSPATH=`cygpath --path --dos "$SOAPUI_CLASSPATH"`
fi

cd $DIRNAME
echo ================================
echo =
echo = SOAPUI_HOME = $SOAPUI_HOME
echo = SOAPUI_CLASSPATH = $SOAPUI_CLASSPATH
echo =
echo ================================

SORCER_BOOT_CP=$MVN_REPO/org/sorcersoft/sorcer/sorcer-lib/$V_SORCER/sorcer-lib-$V_SORCER.jar:\
$MVN_REPO/org/sorcersoft/sorcer/ex0-api/$V_SORCER/ex0-api-$V_SORCER.jar:\
$MVN_REPO/org/sorcersoft/sorcer/ex0-api/$V_SORCER/ex0-api-$V_SORCER.jar:\

java $JAVA_OPTS -cp $SOAPUI_CLASSPATH:$SORCER_BOOT_CP com.eviware.soapui.SoapUI "$@"
