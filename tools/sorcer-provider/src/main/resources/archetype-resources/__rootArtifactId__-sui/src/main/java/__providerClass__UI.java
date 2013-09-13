package ${package};

import net.jini.core.lookup.ServiceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.provider.Provider;
import sorcer.service.Service;

import javax.swing.*;
import java.awt.*;

public class ${providerClass}UI extends JPanel {

	private final static Logger logger = LoggerFactory.getLogger(${providerClass}UI.class);

	private ServiceItem item;
    // This variable gives access to the provider who invoked this UI.
	private Service provider;

	public ${providerClass}UI(Object obj) {
		super();
		getAccessibleContext().setAccessibleName("${providerInterface} UI");
		try {
			item = (ServiceItem) obj;
			logger.info("service class: {} service object: {}", item.service.getClass(), item.service);

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
        Label label4 = new Label("This implementation is contained in the '${rootArtifactId}-sui' module in the '${providerClass}UI.java' file.");
        add(label3);
        add(label4);
		validate();
	}
}
