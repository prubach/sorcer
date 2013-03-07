#!/bin/sh

WEBSTER="http://${IGRID_WEBSTER}:${IGRID_WEBSTER_PORT}"

echo "Webster: ${WEBSTER}"

REQUESTOR_NAME="fnEvalIfTask"
REQUESTOR_CLASS="sorcer.falcon.examples.requestor.fnEval.legacy.fnEvalIfTask.FnEvalIfTask"

JINI_JARS="${IGRID_HOME}/lib/sorcer.jar:${IGRID_HOME}/lib/jgapp.jar:${IGRID_HOME}/common/jini-ext.jar:${IGRID_HOME}/common/sun-util.jar:${IGRID_HOME}/common/serviceui-1.1.jar"
JEP_JARS="${IGRID_HOME}/common/jep-2.4.0.jar:${IGRID_HOME}/common/ext-1.1.0.jar"
SORCER_JARS="${IGRID_HOME}/lib/sorcer.jar:${IGRID_HOME}/lib/jgapp.jar"

java -classpath ${JINI_JARS}:${SORCER_JARS}:${JEP_JARS}:${IGRID_HOME}/lib/${REQUESTOR_NAME}.jar \
	 -Djava.util.logging.config.file="${IGRID_HOME}/configs/sorcer.logging" \
	 -Djava.security.policy="../policy/${REQUESTOR_NAME}-req.policy" \
	 -Djava.rmi.server.codebase="${WEBSTER}/${REQUESTOR_NAME}-dl.jar" \
     -Dsorcer.env.file="${IGRID_HOME}/configs/sorcer.env" \
     ${REQUESTOR_CLASS}