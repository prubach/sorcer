/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.sorcer.ui;

import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.core.Provider;
import sorcer.core.SorcerEnv;
import sorcer.resolver.Resolver;
import sorcer.service.Service;
import sorcer.ui.serviceui.UIComponentFactory;
import sorcer.ui.serviceui.UIDescriptorFactory;


import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.logging.Logger;

public class SampleUI extends JPanel {

	private static final long serialVersionUID = 3689252561261L;

	private final static Logger logger = Logger.getLogger(SampleUI.class
			.getName());

	private ServiceItem item;
    // This variable gives access to the provider who invoked this UI.
	private Service provider;

	public SampleUI(Object obj) {
		super();
		getAccessibleContext().setAccessibleName("Sample Service");
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
		setBackground(Color.white); // Set white background
		setLayout(new FlowLayout());
		Label label3 = new Label("This is a sample Window that you can extend or replace using your own implementation.");
        Label label4 = new Label("This implementation is contained in the 'first-sui' module in the 'SampleUI.java' file.");
        add(label3);
        add(label4);
		validate();
	}

    /**
     * Returns a service UI descriptor of this service. Usually this method is
     * used as an entry in provider configuration files when smart proxies are
     * deployed with a standard off the shelf {@link sorcer.core.provider.ServiceProvider}.
     *
     * @return service UI descriptor
     */
    public static UIDescriptor getUIDescriptor() {
        UIDescriptor uiDesc = null;
        try {
            URL uiUrl = new URL(SorcerEnv.getWebsterUrl() + "/" + Resolver.resolveRelative("com.example.sorcer:first-sui:1.0-SNAPSHOT"));
            uiDesc = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                    new UIComponentFactory(new URL[] {uiUrl}, SampleUI.class.getName()));
        } catch (Exception ex) {
            logger.throwing("SampleUI", "Problem loading SUI", ex);
        }
        return uiDesc;
    }
}
