package sorcer.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rafał Krupiński
 */
public class RioSorcerFlavour extends SorcerFlavour {

    @Override
    public String getMainClass() {
        return "org.rioproject.start.ServiceStarter";
    }

    @Override
    protected String[] getFlavourSpecificClassPath() {
        return new String[0];
    }

    @Override
    public List<String> getDefaultConfigs() {
        return Arrays.asList("configs/rio/start-all.groovy");
    }

    @Override
    public List<String> getNonResolvableClassPath() {
        return Arrays.asList(new File(System.getProperty("JAVA_HOME"), "lib/tools.jar").getPath());
    }

    @Override
    public OutputConsumer getConsumer() {
        return new RioOutputConsumer();
    }

    static class RioOutputConsumer implements OutputConsumer {
        final private static Logger log = LoggerFactory.getLogger(RioOutputConsumer.class);

        private Pattern startPattern = Pattern.compile(".*Instantiating \\[([\\w/]+), instance:(\\d+)\\]");

        private Pattern stopPatter = Pattern.compile(".*\\[([\\w/]+)\\] service provisioned, instanceId=\\[\\d+\\], type=\\[\\w+\\], have \\[(\\d+)\\].*");


        Set<String> allServices = new HashSet<String>();
        Set<String> startingServices = new HashSet<String>();

        @Override
        public boolean consume(String line) {
            Matcher m = startPattern.matcher(line);
            if (m.find()) {
                initService(m.group(1), m.group(2));
                return true;
            }

            m = stopPatter.matcher(line);
            if (m.find())
                serviceStared(m.group(1));

            return allServices.isEmpty() || !startingServices.isEmpty();
        }

        private void initService(String name, String num) {
            startingServices.add(name);
            allServices.add(name);
            log.info("Starting {} services", allServices.size());
        }

        private void serviceStared(String name) {
            startingServices.remove(name);
            int all = allServices.size();
            int started = all - startingServices.size();
            log.info("Started {}, {} out of {} services", name, started, all);
        }
    }
}
