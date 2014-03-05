package junit.sorcer.core.deploy;

import java.io.File;
import java.io.IOException;
import java.rmi.RMISecurityManager;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.rioproject.deploy.DeployAdmin;
import org.rioproject.monitor.ProvisionMonitor;
import org.rioproject.opstring.OperationalStringManager;

import sorcer.util.Sorcer;
import sorcer.util.exec.ExecUtils;

/**
 * Class
 */
public class DeploySetup {

    @BeforeClass
    public static void verifyIGridRunning() throws Exception {
        long t0 = System.currentTimeMillis();
        ProvisionMonitor monitor = Util.waitForService(ProvisionMonitor.class, 5);
        if(monitor==null) {
            //setUp();
            monitor = Util.waitForService(ProvisionMonitor.class);
        }
        Assert.assertNotNull(monitor);
        System.out.println("Waited "+(System.currentTimeMillis()-t0)+" millis for ProvisionMonitor discovery");
//        DeployAdmin deployAdmin = (DeployAdmin) monitor.getAdmin();
//        OperationalStringManager manager = deployAdmin.getOperationalStringManager("Sorcer OS");
 //       t0 = System.currentTimeMillis();
  //      Util.waitForDeployment(manager);
   //     System.out.println("Waited " + (System.currentTimeMillis() - t0) + " millis for [Sorcer OS] provisioning");
    }
}
