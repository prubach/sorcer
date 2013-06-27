package sorcer.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class ProviderNameUtil {
    protected Map<String, String> names = new HashMap<String, String>();

    {
        names = new PropertiesLoader().loadAsMap(getClass());
    }

	public String getName(Class<?> providerType) {
		return names.get(providerType.getName());
	}
}
