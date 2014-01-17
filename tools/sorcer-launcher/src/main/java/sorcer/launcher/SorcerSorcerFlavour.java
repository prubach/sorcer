package sorcer.launcher;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rafał Krupiński
 */
public class SorcerSorcerFlavour extends SorcerFlavour {
    @Override
    public String getMainClass() {
        return "sorcer.boot.ServiceStarter";
    }

    @Override
    protected String[] getFlavourSpecificClassPath() {
        return new String[]{
                "net.jini:jsk-resources",
                "org.rioproject:rio-lib",
                "org.sorcersoft.sorcer:util-rio",
                //"org.sorcersoft.sorcer:sos-webster",
                "com.google.guava:guava:15.0",
                "commons-io:commons-io"
        };
    }

    @Override
    public List<String> getDefaultConfigs() {
        return Arrays.asList("configs/sorcer-boot.config");
    }

    @Override
    public OutputConsumer getConsumer() {
        return new SorcerOutputConsumer();
    }
}
