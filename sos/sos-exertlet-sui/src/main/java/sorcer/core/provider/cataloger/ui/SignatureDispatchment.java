/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.core.provider.cataloger.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.ListSelectionListener;

import sorcer.core.Provider;
import sorcer.service.Context;

/**
 * The Signature Dispatcher Interface allows us to abstract the interactions
 * with cataloger or the provider. The two implementations of this interface are @link
 * SignatureDispatcherForCataloger and @link SignatureDispatcherForCataloger
 * each implementation handles obtaining the information requested by the model.
 * 
 * @author Greg McChensey
 * 
 */
public interface SignatureDispatchment extends ActionListener {

	/**
	 * Provides a ListSelectionListener for the provider list.
	 * 
	 * @return ListSelectionListener for the provider list
	 */
	ListSelectionListener getProviderListener();

	/**
	 * Provides a ListSelectionListener for the interface list.
	 * 
	 * @return ListSelectionListener for the interface list
	 */
	ListSelectionListener getInterfaceListener();

	/**
	 * Provides a ListSelectionListener for the method list.
	 * 
	 * @return ListSelectionListener for the method list
	 */
	ListSelectionListener getMethodListener();

	/**
	 * This method is called when the user utilizes the search functionality on
	 * the bottom of each list.
	 */
	void actionPerformed(ActionEvent e);

	/**
	 * This method fills the model with the appropriate data.
	 */
	void fillModel();

	/**
	 * Gets the list of providers
	 * 
	 * @return String array of the provider names
	 */
	String[] getProviders();

	/**
	 * Gets the list of interfaces for the given provider
	 * 
	 * @param providerName
	 *            String representing the provider to get the interface list for
	 * @return String array of the interface names
	 */
	String[] getInterfaces(String providerName);

	/**
	 * Gets the list of methods for the given interface and the currently
	 * selected provider
	 * 
	 * @param interfaceName
	 *            String representing the currently selected interface
	 * @return String array of the method names
	 */
	String[] getMethods(String interfaceName);

	/**
	 * Gets the list of contexts currently stored on the provider.
	 * 
	 * @return String array of the currently stored dataContext names
	 */
	String[] getSavedContextList();

	/**
	 * Obtains the dataContext for the specified method name from the network.
	 * 
	 * @param methodName
	 *            String representing the method to obtain the dataContext from
	 * @return the service dataContext for the method
	 */
	Context getContext(String methodName);

	/**
	 * Save a dataContext back to the network, saves the dataContext as the currently
	 * selected method name.
	 * 
	 * @param theContext
	 *            Context to be saved.
	 * @return Boolean indicating if the operation was successful.
	 */
	Boolean saveContext(Context theContext);

	/**
	 * Save the dataContext to the network, this stores the dataContext under the name
	 * provided in newName.
	 * 
	 * @param newName
	 *            String representing the name the dataContext should be saved as
	 * @param theContext
	 *            Context to be saved.
	 * @return Boolean indicating if the operation was successful.
	 */
	Boolean saveContext(String newName, Context theContext);

	/**
	 * Delete a dataContext from the network, the dataContext to be deleted is defined
	 * by the String methodName.
	 * 
	 * @param methodName
	 *            String with the name of the dataContext to delete.
	 * @return Boolean indicating if the operation was successful.
	 */
	Boolean deleteContext(String methodName);

	/**
	 * This method creates an exertion using the dataContext provided by the user.
	 * The results of the exertion are returned to the user.
	 * 
	 * @param theContext
	 *            Context to be sent with the exertion
	 * @return Context returned from the exertion.
	 */
	Context exertService(Context theContext);

	
	Provider getProvider();
	
}
