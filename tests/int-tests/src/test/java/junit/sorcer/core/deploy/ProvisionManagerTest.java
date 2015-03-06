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
package junit.sorcer.core.deploy;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.rioproject.deploy.DeployAdmin;
import org.rioproject.monitor.ProvisionMonitor;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;

/**
 * @author Dennis Reedy
 */
@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
//@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api"})
//@SorcerServiceConfiguration(":ex6-cfg-all")
public class ProvisionManagerTest extends DeploySetup {

    @Test
    public void testDeploy() throws Exception {
    	long t0 = System.currentTimeMillis();
        ProvisionMonitor monitor = Util.waitForService(ProvisionMonitor.class);
        Assert.assertNotNull(monitor);
        System.out.println("Waited "+(System.currentTimeMillis()-t0)+" millis for ProvisionMonitor discovery");
        DeployAdmin deployAdmin = (DeployAdmin) monitor.getAdmin();
      /*  OperationalStringManager manager = deployAdmin.getOperationalStringManager("Sorcer OS");
        Util.waitForDeployment(manager);
        Job f1 = Util.createJob();
        ProvisionManager provisionManager = new ProvisionManager(f1);
        Assert.assertTrue(provisionManager.deployServices());        */
    }
}
