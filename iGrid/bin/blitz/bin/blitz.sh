JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Home
JINI_HOME=/Users/sobol/jini2_1/lib/
START_CONFIG=config/start-blitz.config

$JAVA_HOME/bin/java -Djava.security.policy=configs/sorcer.policy -jar $JINI_HOME/start.jar $START_CONFIG
