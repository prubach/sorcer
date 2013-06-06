package sorcer.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rafał Krupiński
 */
public class ProviderNameUtil {
	private Map<String, String> names = new HashMap<String, String>();
	{
		names = new PropertiesLoader().loadAsMap(ProviderNameUtil.class);
	}

	public String getName(Class<?> providerType) {
		return names.get(providerType.getName());
	}
}
