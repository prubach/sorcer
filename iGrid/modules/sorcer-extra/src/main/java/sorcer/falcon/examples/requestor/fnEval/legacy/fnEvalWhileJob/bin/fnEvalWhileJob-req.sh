#!/bin/sh

WEBSTER="http://${IGRID_WEBSTER}:${IGRID_WEBSTER_PORT}"

echo "Webster: ${WEBSTER}"

REQUESTOR_NAME="fnEvalWhileJob"
REQUESTOR_CLASS="sorcer.falcon.examples.requestor.fnEval.legacy.fnEvalWhileJob.FnEvalWhileJob"

JINI_JARS="${SORCER_HOME}/lib/sorcer.jar:${SORCER_HOME}/lib/jgapp.jar:${SORCER_HOME}/common/jini-ext.jar:${SORCER_HOME}/common/sun-util.jar:${SORCER_HOME}/common/serviceui-1.1.jar"
JEP_JARS="${SORCER_HOME}/common/jep-2.4.0.jar:${SORCER_HOME}/common/ext-1.1.0.jar"
SORCER_JARS="${SORCER_HOME}/lib/sorcer.jar:${SORCER_HOME}/lib/jgapp.jar"

java -classpath ${JINI_JARS}:${SORCER_JARS}:${JEP_JARS}:${SORCER_HOME}/lib/${REQUESTOR_NAME}.jar \
	 -Djava.util.logging.config.file="${SORCER_HOME}/configs/sorcer.logging" \
	 -Djava.security.policy="../policy/${REQUESTOR_NAME}-req.policy" \
	 -Djava.rmi.server.codebase="${WEBSTER}/${REQUESTOR_NAME}-dl.jar" \
     -Dsorcer.env.file="${SORCER_HOME}/configs/sorcer.env" \
     ${REQUESTOR_CLASS}