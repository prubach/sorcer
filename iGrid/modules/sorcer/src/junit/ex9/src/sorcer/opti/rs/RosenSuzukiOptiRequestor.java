package sorcer.opti.rs;

import static sorcer.eo.operator.provider;
import static sorcer.eo.operator.sig;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import net.jini.core.event.EventRegistration;
import sorcer.core.context.model.explore.ContextEvent;
import sorcer.core.context.model.opti.Optimization;
import sorcer.util.Log;


public class RosenSuzukiOptiRequestor {

	private static Logger logger = Log.getTestLog();
	
	public static void main(String[] args) throws Exception {
		System.setSecurityManager(new RMISecurityManager());
		logger.info("running: " + args[0]);
		RosenSuzukiOptiRequestor requestor = new RosenSuzukiOptiRequestor();
		if (args[0].equals("register"))
			requestor.register();
	}
	
	private void register() throws Exception {
		Optimization provider = (Optimization)provider(sig("register", Optimization.class));
		long eventID = ContextEvent.getEventID();
		EventRegistration registration = provider.register(eventID, null, null, 1);
		logger.info(">>>>>>>>>>>>> registration: " + registration);
		logger.info(">>>>>>>>>>>>> registration source: " + registration.getSource());
	}
	
}
