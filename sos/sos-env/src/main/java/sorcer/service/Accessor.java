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
package sorcer.service;

import net.jini.core.lookup.ServiceItem;
import sorcer.core.SorcerConstants;
import sorcer.core.SorcerEnv;

import java.util.logging.Logger;

/**
 * A service accessing facility that allows to find dynamically a network
 * service provider matching its {@link Signature}. This class uses the Factory
 * Method pattern with the {@link DynamicAccessor} interface.
 * 
 * @author Mike Sobolewski
 */
public class Accessor {
	
	protected final static Logger logger = Logger.getLogger("sorcer.core");

    /**
	 * A factory returning instances of {@link Service}s.
	 */
	private static DynamicAccessor accessor;

    static {
        initialize(SorcerEnv.getProperties().getProperty(SorcerConstants.S_SERVICE_ACCESSOR_PROVIDER_NAME));
    }

    public static void initialize(String providerType) {
        try {
            logger.fine("* SORCER DynamicAccessor provider: " + providerType);
            Class type = Class.forName(providerType);
            accessor = (DynamicAccessor) type.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No service accessor available for: " + providerType,e);
        } catch (InstantiationException e) {
            throw new RuntimeException("No service accessor available for: " + providerType,e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("No service accessor available for: " + providerType,e);
        }
    }

    /**
	 * Returns a servicer matching its {@link Signature} using the particular
	 * factory <code>accessor</code> of this service accessor facility.
	 * 
	 * @param signature
	 *            the signature of requested servicer
	 * @return the requested {@link Service}
	 * @throws SignatureException 
	 */
	public static Service getServicer(Signature signature)
			throws SignatureException {
		logger.fine("using accessor: " + accessor);
		return accessor.getServicer(signature);
	}

	/**
	 * Returns a service item containing the servicer matching its {@link Signature} 
	 * using the particular factory <code>accessor</code> of this service accessor facility.
	 * 
	 * @param signature
	 *            the signature of requested servicer
	 * @return the requested {@link ServiceItem}
	 * @throws SignatureException 
	 */
	public static ServiceItem getServicerItem(Signature signature) throws SignatureException {
		logger.fine("using accessor: " + accessor);
		return accessor.getServiceItem(signature);
	}


    /**
	 * Returns the current servicer accessor.
	 * 
	 * @return the servicer accessor
	 */
	public static DynamicAccessor getAccessor() {
		return accessor;
	}

    public static <T> T getProvider(String name, Class<T> type){
        return (T) getAccessor().getProvider(name, type);
    }
}