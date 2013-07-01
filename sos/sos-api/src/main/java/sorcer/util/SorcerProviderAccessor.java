package sorcer.util;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace05;
import sorcer.core.Provider;
import sorcer.core.SorcerNotifierProtocol;
import sorcer.service.Accessor;
import sorcer.service.Jobber;
import sorcer.service.Spacer;
import sorcer.service.jobber.JobberAccessor;
import sorcer.service.space.SpaceAccessor;
import sorcer.service.spacer.SpacerAccessor;
import sorcer.service.txmgr.TransactionManagerAccessor;

/**
 * @author Rafał Krupiński
 */
public class SorcerProviderAccessor extends ServiceAccessor{

    /**
     * Returns any SORCER Jobber service provider.
     *
     * @return a SORCER Jobber provider
     * @throws sorcer.util.AccessorException
     */
    public static Jobber getJobber() throws AccessorException {
        return JobberAccessor.getJobber();
}

    /**
     * Returns a SORCER Jobber service provider using Jini lookup and discovery.
     *
     * @param name
     *            the name of a Jobber service provider
     * @return a Jobber proxy
     */
    public static Jobber getJobber(String name) {
        return JobberAccessor.getJobber(name);
    }

    /**
     * Returns any SORCER Spacer service provider.
     *
     * @return a SORCER Spacer provider
     * @throws sorcer.util.AccessorException
     */
    public static Spacer getSpacer() throws AccessorException {
        return SpacerAccessor.getSpacer();
    }

    /**
     * Returns a SORCER Spacer service provider using Jini lookup and discovery.
     *
     * @param name
     *            the name of a spacer service provider
     * @return a Spacer proxy
     */
    public static Spacer getSpacer(String name) {
        return SpacerAccessor.getSpacer(name);
    }

    public static Provider getNotifierProvider() throws ClassNotFoundException {
        return Accessor.getProvider(null, SorcerNotifierProtocol.class);
    }

    /**
     * Returns a Jini transaction manager service.
     *
     * @return Jini transaction manager
     */
    public static TransactionManager getTransactionManager() {
        return TransactionManagerAccessor.getTransactionManager();
    }

    /**
     * Returns a JavaSpace service with a given name.
     *
     * @return JavaSpace proxy
     */
    public static JavaSpace05 getSpace(String spaceName) {
        return SpaceAccessor.getSpace(spaceName);
    }

    /**
     * Returns a JavaSpace service with a given name and group.
     *
     * @return JavaSpace proxy
     */
    public static JavaSpace05 getSpace(String spaceName, String spaceGroup) {
        return SpaceAccessor.getSpace(spaceName, spaceGroup);
    }

    /**
     * Returns a Jini JavaSpace service.
     *
     * @return Jini JavaSpace
     */
    public static JavaSpace05 getSpace() {
        return SpaceAccessor.getSpace();
    }

}
