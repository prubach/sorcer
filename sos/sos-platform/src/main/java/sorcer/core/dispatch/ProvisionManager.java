/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.rioproject.deploy.DeployAdmin;
import org.rioproject.monitor.ProvisionMonitor;
import org.rioproject.opstring.OpString;
import org.rioproject.opstring.OpStringLoader;
import org.rioproject.opstring.OperationalString;
import org.rioproject.opstring.OperationalStringManager;
import org.rioproject.opstring.ServiceElement;

import sorcer.core.exertion.NetTask;
import sorcer.core.provider.Spacer;
import sorcer.core.signature.NetSignature;
import sorcer.ext.Provisioner;
import sorcer.service.Accessor;
import sorcer.service.Exertion;
import sorcer.service.Service;
import sorcer.service.Signature;
import sorcer.util.Sorcer;

/**
 * @author Pawel Rubach
 */
public class ProvisionManager {
	private static final Logger logger = Logger.getLogger(ProvisionManager.class.getName());
	protected Set<SignatureElement> servicesToProvision = new LinkedHashSet<SignatureElement>();
    private static ProvisionManager instance = null;


    public static ProvisionManager getInstance() {
        if (instance==null)
            instance = new ProvisionManager();
        return instance;
    }

	
	protected ProvisionManager() {
        ThreadGroup provGroup = new ThreadGroup("spacer-provisioning");
        provGroup.setDaemon(true);
        provGroup.setMaxPriority(Thread.NORM_PRIORITY - 1);
        ProvisionThread pThread = new ProvisionThread(provGroup);
        pThread.start();
	}

    public void add(Signature sig) {
        Service service = (Service) Accessor.getService(sig);
        // A hack to disable provisioning spacer itself
        if (service==null && !sig.getServiceType().getName().equals(Spacer.class.getName())) {
            synchronized (servicesToProvision) {
                servicesToProvision.add(
                        new SignatureElement(sig.getServiceType().getName(), sig.getProviderName(),
                                ((NetSignature)sig).getVersion(), sig));
            }
        }
    }



    protected class ProvisionThread extends Thread {

        public ProvisionThread(ThreadGroup disatchGroup) {
            super(disatchGroup, "Provisioner");
        }

        public void run() {
            Provisioner provisioner = Accessor.getService(Provisioner.class);
            while (true) {
                if (!servicesToProvision.isEmpty()) {
                    Iterator<SignatureElement> it = servicesToProvision.iterator();
                    Set<SignatureElement> sigsToRemove = new LinkedHashSet<SignatureElement>();
                    logger.fine("Services to provision from Spacer/Jobber: "+ servicesToProvision.size());

                    while (it.hasNext()) {
                        SignatureElement sigEl = it.next();

                        // Catalog lookup or use Lookup Service for the particular
                        // service
                        Service service = (Service) Accessor.getService(sigEl.getSignature());
                        if (service == null ) {
                            if (provisioner != null) {
                                try {
                                    logger.info("Provisioning: "+ sigEl.getSignature());
                                    service = provisioner.provision(sigEl.getServiceType(), sigEl.getProviderName(), sigEl.getVersion());
                                    if (service!=null) sigsToRemove.add(sigEl);
                                } catch (RemoteException re) {
                                    provisioner = Accessor.getService(Provisioner.class);
                                    String msg = "Problem provisioning "+sigEl + " " +re.getMessage();
                                    logger.severe(msg);
                                    //throw new ProvisioningException(msg, ((NetSignature)sig).getExertion());
                                }
                            }
                        } else
                            sigsToRemove.add(sigEl);
                    }
                    if (!sigsToRemove.isEmpty()) {
                        synchronized (servicesToProvision) {
                            servicesToProvision.removeAll(sigsToRemove);
                        }
                    }
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }
    }


    private class SignatureElement {
        String serviceType;
        String providerName;
        String version;
        Signature signature;

        private String getServiceType() {
            return serviceType;
        }

        private void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        private String getProviderName() {
            return providerName;
        }

        private void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        private String getVersion() {
            return version;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        private Signature getSignature() {
            return signature;
        }

        private void setSignature(Signature signature) {
            this.signature = signature;
        }

        private SignatureElement(String serviceType, String providerName, String version, Signature signature) {
            this.serviceType = serviceType;
            this.providerName = providerName;
            this.version = version;
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SignatureElement that = (SignatureElement) o;
            if (!providerName.equals(that.providerName)) return false;
            if (!serviceType.equals(that.serviceType)) return false;
            if (version != null ? !version.equals(that.version) : that.version != null) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = serviceType.hashCode();
            result = 31 * result + providerName.hashCode();
            result = 31 * result + (version != null ? version.hashCode() : 0);
            return result;
        }
    }

}
