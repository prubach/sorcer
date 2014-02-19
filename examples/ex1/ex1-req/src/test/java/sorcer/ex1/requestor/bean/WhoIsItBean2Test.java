package sorcer.ex1.requestor.bean;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerEnv;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.ex1.requestor.RequestorMessage;
import sorcer.junit.*;
import sorcer.service.Context;
import sorcer.service.Task;

import java.net.InetAddress;

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
public class WhoIsItBean2Test {
    private final static Logger logger = LoggerFactory.getLogger(WhoIsItBean2Test.class);

    @Test
    public void getExertion() throws Throwable {
		String hostname, ipAddress;
		InetAddress inetAddress;
        String providerName = SorcerEnv.getActualName("ABC");
		Context context = null;
		NetSignature signature = null;
		// define requestor data
		logger.info("providerName: " + providerName);
		Task task = null;
        inetAddress = InetAddress.getLocalHost();

        hostname = inetAddress.getHostName();
        ipAddress = inetAddress.getHostAddress();

        context = new ServiceContext("Who Is It?");
        context.putValue("requestor/message", new RequestorMessage(
                "WhoIsIt Bean"));
        context.putValue("requestor/hostname", hostname);
        context.putValue("requestor/address", ipAddress);

        signature = new NetSignature("getHostName",
                sorcer.ex1.WhoIsIt.class, providerName);

        task = new NetTask("Who Is It?", signature, context);
        task.exert();
        ExertionErrors.check(task.getExceptions());
    }
}
