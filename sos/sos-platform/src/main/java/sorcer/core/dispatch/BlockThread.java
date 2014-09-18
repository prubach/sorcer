/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.core.dispatch;

import net.jini.config.ConfigurationException;
import sorcer.core.DispatchResult;
import sorcer.core.Dispatcher;
import sorcer.core.provider.Provider;
import sorcer.service.Block;

import java.rmi.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static sorcer.util.StringUtils.tName;

public class BlockThread extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(BlockThread.class
			.getName());

	private static final int SLEEP_TIME = 250;
	// doBlock method calls internally
	private Block block;

	private Block result;

	Provider provider;

	public BlockThread(Block block, Provider provider) {
        super(tName("Block-" + block));
		this.block = block;
		this.provider = provider;
	}

	public void run() {
		logger.debug("*** Exertion dispatcher started with control context ***\n"
				+ block.getControlContext());
		try {
            String exertionDeploymentConfig = null;
			if (block.isProvisionable()) {
				try {
					exertionDeploymentConfig = 
							(String) provider.getProviderConfiguration().getEntry("sorcer.core.provider.ServiceProvider",
									"exertionDeploymentConfig", 
									String.class, 
									null);
				} catch (ConfigurationException e1) {
					logger.warn("Unable to read property from configuration", e1);
                } catch (RemoteException e) {
             				// ignore it, locall call
				}
			}
            Dispatcher dispatcher = ExertionDispatcherFactory.getFactory().createDispatcher(block, provider, exertionDeploymentConfig);

			try {
				block.getControlContext().appendTrace(provider.getProviderName() +
						" dispatcher: " + dispatcher.getClass().getName());
			} catch (RemoteException e) {
				// ignore it, locall call
			}
            dispatcher.exec();
            DispatchResult result = dispatcher.getResult();

			logger.debug("*** Dispatcher exit state = " + dispatcher.getClass().getName()  + " state: " + result.state
					+ " for block***\n" + block.getControlContext());
            this.result = (Block) result.exertion;
		} catch (DispatcherException de) {
            logger.warn("Error while exerting " + block.getId(), de);
        }
	}

	public Block getBlock() {
		return block;
	}

	public Block getResult() {
		return result;
	}
}
