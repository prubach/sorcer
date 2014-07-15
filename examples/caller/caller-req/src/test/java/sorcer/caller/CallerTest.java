package sorcer.caller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.PositionalContext;
import sorcer.junit.*;
import sorcer.service.*;

import java.rmi.RMISecurityManager;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static sorcer.eo.operator.*;

@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:caller-dl:pom:1.0-SNAPSHOT"})
@SorcerServiceConfiguration(":caller-cfg")
public class CallerTest {

    private static Logger logger = LoggerFactory.getLogger(CallerTest.class);

    @Test
    public void testCaller() throws ContextException, SignatureException, ExertionException {
        logger.info("Starting CallerTest");

        System.setSecurityManager(new RMISecurityManager());
        logger.info("Starting CallerTester");

        Context ctx = new PositionalContext("caller");
        String[] comms = new String[] { "java" };
        String[] argss = new String[] { "-version" };
        CallerUtil.setCmds(ctx, comms);
        CallerUtil.setArgs(ctx, argss);
        CallerUtil.setBin(ctx);
        CallerUtil.setWorkingDir(ctx, "/");

        Task t1 = task("test", sig("execute", Caller.class), ctx);

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);
        logger.info("Got result: " + CallerUtil.getCallOutput(ctx));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: {}" + trace(out));

        assertNotNull(CallerUtil.getCallOutput(ctx));
    }
}
