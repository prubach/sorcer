package sorcer.core.provider.outrigger;

import sorcer.config.ConfigEntry;

import java.util.Set;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerConfiguration {
    @ConfigEntry
    public boolean workerTransactional;

    @ConfigEntry
    public boolean matchInterfaceOnly;

    @ConfigEntry
    public boolean spaceEnabled;

    public Set<Class> interfaces;

    public boolean available;
}
