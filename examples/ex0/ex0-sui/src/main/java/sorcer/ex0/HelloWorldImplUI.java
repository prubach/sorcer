package sorcer.ex0;

import net.jini.core.lookup.ServiceItem;
import sorcer.core.provider.Provider;
import sorcer.service.Service;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class HelloWorldImplUI extends JPanel {

	private final static Logger logger = Logger.getLogger(HelloWorldImplUI.class
			.getName());

	private ServiceItem item;
    // This variable gives access to the provider who invoked this UI.
	private Service provider;

	public HelloWorldImplUI(Object obj) {
		super();
		getAccessibleContext().setAccessibleName("HelloWorld UI");
		try {
			item = (ServiceItem) obj;
			logger.info("service class: " + item.service.getClass().getName()
					+ "\nservice object: " + item.service);

			if (item.service instanceof Provider) {
				provider = (Provider) item.service;
			}

            SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createUI();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void createUI() {
		setBackground(Color.yellow); // Set white background
		setLayout(new FlowLayout());
		Label label3 = new Label("This is a sample Window that you can extend or replace using your own implementation.");
        Label label4 = new Label("This implementation is contained in the 'ex0-sui' module in the 'HelloWorldImplUI.java' file.");
        add(label3);
        add(label4);
		validate();
    }
}
