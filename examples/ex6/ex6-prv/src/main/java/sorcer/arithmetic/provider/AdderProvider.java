package sorcer.arithmetic.provider;

import java.net.URL;
import java.rmi.RemoteException;

import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.arithmetic.provider.ui.CalculatorUI;
import sorcer.core.SorcerEnv;
import sorcer.core.provider.ServiceTasker;
import sorcer.core.provider.exertmonitor.MonitorClient;
import sorcer.service.*;
import sorcer.ui.serviceui.UIComponentFactory;
import sorcer.ui.serviceui.UIDescriptorFactory;

import com.sun.jini.start.LifeCycle;

public class AdderProvider extends ServiceTasker implements RemoteAdder {
	private Arithmometer arithmometer = new Arithmometer();

    private MonitorClient monitorClient = new MonitorClient();

	public AdderProvider(String[] args, LifeCycle lifeCycle) throws Exception {
		super(args, lifeCycle);
	}
	
	public Context add(Context context) throws RemoteException,
			ContextException, MonitorException {
		Context out = arithmometer.add(context);		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        monitorClient.checkpoint(out);
		return out;
	}
	
	public static UIDescriptor getCalculatorDescriptor() {
		UIDescriptor uiDesc = null;
		try {
			uiDesc = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
					new UIComponentFactory(new URL[] { new URL(SorcerEnv
							.getWebsterUrl()
							+ "/calculator-ui.jar") }, CalculatorUI.class
							.getName()));
		} catch (Exception ex) {
			// do nothing
		}
		return uiDesc;
	}
}
