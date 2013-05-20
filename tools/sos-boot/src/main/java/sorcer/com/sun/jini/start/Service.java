package sorcer.com.sun.jini.start;

import com.sun.jini.start.ServiceDescriptor;

/**
 * Trivial class used as the return value by the
 * <code>create</code> methods. This class aggregates
 * the results of a service creation attempt:
 * proxy (if any), exception (if any), associated
 * descriptor object.
 *
 *
 * Extracted from internal class ServiceStarter.Result
 */
public class Service {
	/**
	 * Service proxy object, if any.
	 */
	public final Object result;
	/**
	 * Service creation exception, if any.
	 */
	public final Exception exception;
	/**
	 * Associated <code>ServiceDescriptor</code> object
	 * used to create the service instance
	 */
	public final ServiceDescriptor descriptor;

	/**
	 * Trivial constructor. Simply assigns each argument
	 * to the appropriate field.
	 */
	Service(ServiceDescriptor d, Object o, Exception e) {
		descriptor = d;
		result = o;
		exception = e;
	}

	// javadoc inherited from super class
	public String toString() {
		return this.getClass() + ":[descriptor=" + descriptor + ", "
				+ "result=" + result + ", exception=" + exception + "]";
	}
}
