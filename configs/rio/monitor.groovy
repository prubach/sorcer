/*
 * Copyright to the original author or authors.
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

/*
 * Configuration for a Provision Monitor
 */
import net.jini.core.discovery.LookupLocator
import net.jini.export.Exporter
import net.jini.jrmp.JrmpExporter
import net.jini.jeri.*;
import net.jini.jeri.tcp.*;

import org.rioproject.config.Component
import org.rioproject.config.Constants
import org.rioproject.monitor.service.selectors.LeastActiveSelector
import org.rioproject.monitor.service.selectors.ServiceResourceSelector
import org.rioproject.resolver.RemoteRepository
import org.rioproject.impl.client.JiniClient
import net.jini.security.BasicProxyPreparer
import net.jini.core.constraint.InvocationConstraints
import net.jini.constraint.BasicMethodConstraints
import net.jini.core.constraint.ConnectionRelativeTime
import net.jini.security.ProxyPreparer
import net.jini.core.constraint.MethodConstraints
import net.jini.core.entry.Entry
import org.rioproject.util.PortUtil
import org.rioproject.entry.UIDescriptorFactory
import org.rioproject.RioVersion
import net.jini.lookup.ui.MainUI
import org.rioproject.serviceui.UIFrameFactory
import sorcer.core.SorcerEnv
import sorcer.util.StringUtils;

/*
* Declare Provision Monitor properties
*/
@Component('org.rioproject.monitor')
class MonitorConfig {
    String serviceName = 'Provision Monitor'
    String serviceComment = 'Dynamic Provisioning Agent'
    String jmxName = 'org.rioproject.monitor:type=Monitor';

    String[] getInitialOpStrings() {
        return StringUtils.tokenizerSplit(System.getProperty(sorcer.core.SorcerConstants.P_MONITOR_INITIAL_OPSTRINGS, ""), File.pathSeparator);
    }

    String[] getInitialLookupGroups() {
        return SorcerEnv.getLookupGroups() as String[]
    }

    RemoteRepository[] getRemoteRepositories() {
        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setId(serviceName.replaceAll(" ", ""))
        remoteRepository.setUrl(SorcerEnv.getWebsterUrl())
        remoteRepository.setSnapshotChecksumPolicy(RemoteRepository.CHECKSUM_POLICY_IGNORE)
        remoteRepository.setReleaseChecksumPolicy(RemoteRepository.CHECKSUM_POLICY_IGNORE)
        def repositories = [remoteRepository]
        return repositories as RemoteRepository[]
    }

    LookupLocator[] getInitialLookupLocators() {
        String locators = System.getProperty(Constants.LOCATOR_PROPERTY_NAME)
        if(locators!=null) {
            def lookupLocators = JiniClient.parseLocators(locators)
            return lookupLocators as LookupLocator[]
        } else {
            return null
        }
    }

    ServiceResourceSelector getServiceResourceSelector() {
        return new LeastActiveSelector()
    }

    Entry[] getServiceUIs(String codebase) {
        String uiClass = 'org.rioproject.tools.ui.ServiceUIWrapper'
        URL url = new URL("artifact:org.rioproject:rio-ui:${RioVersion.VERSION}")
        def entry = [UIDescriptorFactory.getUIDescriptor(MainUI.ROLE, new UIFrameFactory(url, uiClass))]
        return entry as Entry[]
    }

    /*
     * Use a JrmpExporter for the OpStringManager.
     *
     * PR - there is a problem when it is started on a computer with no dns (127.x)
     * therefore it is changed to BasicJeriExporter
     */
    Exporter getOpStringManagerExporter() {
        int port = 0
        String portRange = System.getProperty(Constants.PORT_RANGE)
        if(portRange!=null)
            port = PortUtil.getPortFromRange(portRange)
        return new BasicJeriExporter(TcpServerEndpoint.getInstance(SorcerEnv.getHostAddress(), 0), new BasicILFactory());
        //return new JrmpExporter(port)
    }
    
    ProxyPreparer getInstantiatorPreparer() {
        MethodConstraints serviceListenerConstraints =
                new BasicMethodConstraints(new InvocationConstraints(new ConnectionRelativeTime(30000),
                                                                     null))
        return  new BasicProxyPreparer(false, serviceListenerConstraints, null);        
    }
}
