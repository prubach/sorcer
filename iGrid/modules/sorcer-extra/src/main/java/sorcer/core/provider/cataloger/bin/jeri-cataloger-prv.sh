#!/bin/sh

# Replace MYPROVIDER with your provider name and specify property value for "PROVIDER_CLASS"
# Also you might need to adjust the classpath (provider.classpath) for your provider.

WEBSTER="http://${SORCER_WEBSTER}:${SORCER_WEBSTER_PORT}"

PROVIDER_NAME="cataloger"
PROVIDER_CLASS="sorcer.core.provider.cataloger.CatalogerImpl"

JINI_JARS="${SORCER_HOME}/common/sun-util.jar:${SORCER_HOME}/common/jini-ext.jar:${SORCER_HOME}/common/serviceui-1.1.jar"
SORCER_JARS="${SORCER_HOME}/lib/sorcer.jar"
JINI_DL="${WEBSTER}/jini-ext.jar ${WEBSTER}/serviceui-1.1.jar ${WEBSTER}/sun-util.jar"

echo "SORCER_HOME: ${SORCER_HOME}"
echo "Webster: ${WEBSTER}"

java -Djava.util.logging.config.file=${SORCER_HOME}/configs/sorcer.logging \
     -Djava.security.policy=../policy/${PROVIDER_NAME}-prv.policy \
     -Dsorcer.provider.codebase="${JINI_DL} ${WEBSTER}/${PROVIDER_NAME}-dl.jar ${WEBSTER}/${PROVIDER_NAME}-ui.jar ${WEBSTER}/provider-ui.jar" \
     -Dsorcer.provider.classpath="${JINI_JARS}:${SORCER_JARS}:${SORCER_HOME}/lib/${PROVIDER_NAME}.jar" \
     -Dsorcer.provider.config="../configs/jeri-${PROVIDER_NAME}-prv.config" \
     -Dsorcer.env.file="${SORCER_HOME}/configs/sorcer.env" \
     -Dsorcer.provider.impl="${PROVIDER_CLASS}" \
     -jar ${SORCER_HOME}/common/start.jar ${SORCER_HOME}/configs/startup-prv.config
