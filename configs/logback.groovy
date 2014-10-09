/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.management.ManagementFactory

//installJUL();

/* Scan for changes every minute. */
scan()
jmxConfigurator()
def appenders = prepareAppenders()

def exertMonitor = "ExertMonitor"
def cataloger = "ServiceCataloger"
def concatenator = "ServiceConcatenator"
def jobber = "ServiceJobber"
def spacer = "ServiceSpacer"
def tasker = "ServiceTasker"
def remoteLogger = "RemoteLoggerManager";
def databaseProvider = "DatabaseProvider";
def almanac = "Almanac";
def bazaar = "Bazaar";

for (def service : [exertMonitor, cataloger, concatenator, jobber, spacer, tasker, remoteLogger, databaseProvider, almanac, bazaar]){
    mkAppender(service);
}

/* Set up loggers */
//logger("org.rioproject.cybernode", INFO)
//logger("org.rioproject.cybernode.loader", INFO)
//logger("org.rioproject.config", INFO)
//logger("org.rioproject.resources.servicecore", INFO)
//logger("org.rioproject.system", INFO)
//logger("org.rioproject.cybernode.ServiceBeanLoader", INFO)
//logger("org.rioproject.system.measurable", INFO)
//logger("org.rioproject.jsb", INFO)
//logger("org.rioproject.associations", INFO)

//logger("org.rioproject.monitor", INFO)
//logger("org.rioproject.monitor.sbi", INFO)
//logger("org.rioproject.monitor.provision", INFO)
//logger("org.rioproject.monitor.selector", INFO)
//logger("org.rioproject.monitor.services", INFO)
//logger("org.rioproject.monitor.DeploymentVerifier", INFO)
//logger("org.rioproject.monitor.InstantiatorResource", INFO)
//logger("org.rioproject.monitor.managers.FixedServiceManager", INFO)
//logger("org.rioproject.resolver.aether", DEBUG)
//logger("org.rioproject.rmi.ResolvingLoader", OFF)
//logger("org.rioproject.resolver.ResolverHelper", DEBUG)
logger("org.rioproject.resolver.aether.util.ConsoleRepositoryListener", WARN)
logger("org.rioproject.impl.opstring", WARN)

//logger("org.rioproject.gnostic", INFO)
//logger("org.rioproject.gnostic.drools", INFO)
//logger("org.rioproject.gnostic.DroolsCEPManager", INFO)
//logger("org.rioproject.config.GroovyConfig", INFO)

/*
logger("net.jini.discovery.LookupDiscovery", debug)
logger("net.jini.lookup.JoinManager", debug)
logger("net.jini.reggie", debug)
*/

logger("org.dancres.blitz", WARN)
logger("org.dancres.blitz.disk.SleeveCache", OFF)

logger("sorcer.core.provider.exertmonitor", info, [exertMonitor])
logger("sorcer.core.provider.rendezvous.ServiceConcatenator", info, [concatenator])
logger("sorcer.core.provider.rendezvous.ServiceJobber", info, [jobber])
logger("sorcer.core.provider.rendezvous.ServiceSpacer", info, [spacer])
logger("sorcer.core.provider.ServiceTasker", info, [tasker])
logger("sorcer.core.provider.logger", info, [remoteLogger])
//logger("sorcer.core.provider.dbp", debug, [databaseProvider])

// do not log to the main logger ( additive=false)
logger("sorcer.core.provider.cataloger", info, [cataloger], false)

//commercial
logger("com.sorcersoft.almanac", info, [almanac])
logger("com.sorcersoft.bazaar", info, [bazaar])

//logger("sorcer.core.security", OFF)
logger("sorcer.test", DEBUG)
logger("private", DEBUG)
logger("sorcer.arithmetic", DEBUG)

//logger("sorcer.core.provider.logger", DEBUG)
//logger("sorcer.platform.logger", DEBUG)
//logger("sorcer.core.dispatch", DEBUG)

logger("sorcer.core.invoker.MethodInvoker", INFO)
logger("sorcer.modeling.vfe", DEBUG)

///logger("mil.afrl.mstc.engineering.optimization.conmin.provider", DEBUG)
/*
logger("sorcer.resolver.ProjectArtifactResolver", DEBUG)
logger("sorcer.launcher.SorcerLauncher", DEBUG)
*/

//logger("sorcer.provider.boot", DEBUG)

//logger("sorcer.core.service", debug)
logger("sorcer.tools.webster.start.WebsterStarter", debug)
//logger("sorcer.container.core", debug)


root(INFO, appenders)

/* The following loggers are system watch loggers. When set to debug they will use the WATCH-LOG appender,
 * and have as output values being logged for these particular watches. Uncomment out the loggers you would like
 * to have logged.
 *
 * If you have watches in your service that you want put into a watch-log, then add them as needed, with the
 * logger name of:
 *     "watch.<name of your watch>"
 */
/*
logger("watch.CPU", DEBUG, ["WATCH-LOG"], false)
logger("watch.CPU (Proc)", DEBUG, ["WATCH-LOG"], false)
logger("watch.System Memory", DEBUG, ["WATCH-LOG"], false)
logger("watch.Process Memory", DEBUG, ["WATCH-LOG"], false)
logger("watch.Perm Gen", DEBUG, ["WATCH-LOG"], false)
*/

def prepareAppenders() {
    def appenders = []
    /*
     * Only add the CONSOLE appender if we have a console
     */
    if (System.console() != null && !System.getProperty("os.name").startsWith("Windows")) {
        appender("CONSOLE", ConsoleAppender) {
            withJansi = true

            encoder(PatternLayoutEncoder) {
                pattern = "%highlight(%-5level) %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx"
            }
        }
    } else {
        appender("CONSOLE", ConsoleAppender) {
            encoder(PatternLayoutEncoder) {
                pattern = "%-5level %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx"
            }
        }
    }
    appenders << "CONSOLE"

    appender("ALL", FileAppender) {
        file = getLogDir() + "/all.log"
        encoder(PatternLayoutEncoder) {
            pattern = "%-5level %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx"
        }
    }
    appenders << "ALL"


    /*
     * Only add the rolling file appender if we are logging for a service
     */
    if (System.getProperty("org.rioproject.service") != null) {
        def serviceLogFilename = getLogLocationAndName()

        appender("ROLLING", RollingFileAppender) {
            file = serviceLogFilename + ".log"
            rollingPolicy(TimeBasedRollingPolicy) {

                /* Rollover daily */
                fileNamePattern = "${serviceLogFilename}-%d{yyyy-MM-dd}.%i.log"

                /* Or whenever the file size reaches 10MB */
                timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
                    maxFileSize = "10MB"
                }

                /* Keep 5 archived logs */
                 maxHistory = 5

            }
            encoder(PatternLayoutEncoder) {
                pattern = "%-5level %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx"
            }
        }
        appenders << "ROLLING"
    }

/*
 * The following method call to create the Watch appender must be uncommented if you want to have watches log to a
 * file
 */
// appenders << createWatchAppender()


    return appenders
}

def mkAppender(String service) {
    appender(service, FileAppender) {
        file = new File(getLogDir(), service + ".log").path;

        encoder(PatternLayoutEncoder) {
            pattern = "%-5level %d{HH:mm:ss.SSS} [%t] %logger{36} - %msg%n%rEx"
        }
    }
    return service;
}

private String getLogDir() {
    System.getProperty("RIO_LOG_DIR", new File(
            System.getProperty("sorcer.home", System.getProperty("iGrid.home", System.getenv("SORCER_HOME"))), "logs").path)
}

/**
 * Utility to check if the passed in string ends with a File.separator
 */
def checkEndsWithFileSeparator(String s) {
    if (!s.endsWith(File.separator))
        s = s + File.separator
    return s
}

/**
 * Naming pattern for the output file:
 *
 * a) The output file is placed in the directory defined by the "RIO_LOG_DIR" System property
 * b) With a name based on the "org.rioproject.service" System property.
 * c) The return value from ManagementFactory.getRuntimeMXBean().getName(). This value is expected to have the
 * following format: pid@hostname. If the return includes the @hostname, the @hostname is stripped off.
 */
def getLogLocationAndName() {
    String logDir = checkEndsWithFileSeparator(getLogDir())
    String name = ManagementFactory.getRuntimeMXBean().getName();
    String pid = name;
    int ndx = name.indexOf("@");
    if (ndx >= 1) {
        pid = name.substring(0, ndx);
    }
    return "$logDir${System.getProperty("org.rioproject.service")}-$pid"
}

/**
 * Get the location of the watch.log. If the "RIO_WATCH_LOG_DIR" System property is not set, use
 * the "RIO_HOME" System property appended by /logs
 */
def getWatchLogDir() {
    String watchLogDir = System.getProperty("RIO_WATCH_LOG_DIR")
    if (watchLogDir == null) {
        watchLogDir = checkEndsWithFileSeparator(System.getProperty("RIO_HOME")) + "../../logs"
    }
    return checkEndsWithFileSeparator(watchLogDir)
}

/**
 * This method needs to be called if watch logging is to be used
 */
def createWatchAppender() {
    String watchLogName = getWatchLogDir() + "watches"
    appender("WATCH-LOG", RollingFileAppender) {
        /*
         * In prudent mode, RollingFileAppender will safely write to the specified file,
         * even in the presence of other FileAppender instances running in different JVMs
         */
        prudent = true

        rollingPolicy(TimeBasedRollingPolicy) {

            /* Rollover daily */
            fileNamePattern = "${watchLogName}-%d{yyyy-MM-dd}.%i.log"

            /* Or whenever the file size reaches 10MB */
            timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
                maxFileSize = "10MB"
            }

            /* Keep 5 archived logs */
            maxHistory = 5

        }
        encoder(PatternLayoutEncoder) {
            pattern = "%msg%n"
        }
    }
}

/*def installJUL() {
    def listener = new ch.qos.logback.classic.jul.LevelChangePropagator();
    listener.setContext(context);
    listener.setResetJUL(true);
    org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
    ((ch.qos.logback.classic.LoggerContext) context).addListener(listener);
}*/
