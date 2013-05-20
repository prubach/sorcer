package sorcer.boot;

import sorcer.com.sun.jini.start.Service;

/**
 * @author Rafał Krupiński
 */
public abstract class AbstractServiceStarter {
	public abstract Service[] startServices(String[] args);
}
