package sorcer.core.provider.outrigger;

import sorcer.config.ConfigEntry;

/**
 * @author Rafał Krupiński
 */
public class SpaceTakerConfiguration2 {
    @ConfigEntry(value = "workerTransactional", required = false)
    public boolean workerTransactional;

    @ConfigEntry(value = "matchInterfaceOnly", required = false)
    public boolean matchInterfaceOnly;

    @ConfigEntry(value = "spaceEnabled", required = false)
    public boolean spaceEnabled;
}
