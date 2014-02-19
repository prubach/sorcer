package sorcer.ex1.requestor.bean;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Task;

import java.net.InetAddress;
import java.util.List;

@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:ex1-api",
        "org.sorcersoft.sorcer:ex1-rdl"
})
@SorcerServiceConfigurations({
        @SorcerServiceConfiguration(
                ":ex1-cfg-all"
        ),
        @SorcerServiceConfiguration(
                ":ex1-cfg1"
        )
})
public class WhoIsItBeanRunner1Test {
    private final static Logger logger = LoggerFactory.getLogger(WhoIsItBeanRunner1Test.class);

    @Test
    public void getExertion() throws Throwable {
		String ipAddress;
		InetAddress inetAddress = null;
        String providerName = SorcerEnv.getSuffixedName("ABC");
		logger.info("providerName: " + providerName);
		// define requestor data
		Task task = null;
        inetAddress = InetAddress.getLocalHost();

        ipAddress = inetAddress.getHostAddress();

        Context context = new ServiceContext("Who Is It?");
        context.putValue("requestor/address", ipAddress);

        NetSignature signature = new NetSignature("getHostName",
                sorcer.ex1.WhoIsIt.class, providerName != null ? providerName : null);

        task = new NetTask("Who Is It?", signature, context);
        task.exert();
        ExertionErrors.check(task.getExceptions());
    }
}
