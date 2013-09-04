package sorcer.util;

import sorcer.service.Arg;


public class Deployment implements Arg {
	
	private String name;
	
	private String[] configs;
	
	public Deployment(String... configs) {
		this.configs = configs;
	}
	
	public String[] getConfigs() {
		return configs;
	}

	public void setConfigs(String[] configs) {
		this.configs = configs;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Arg#getName()
	 */
	@Override
	public String getName() {
		return name;
	}


}

	