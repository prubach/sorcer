package sorcer.ex0;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.context.ThrowableTrace;
import sorcer.service.*;
import sorcer.util.junit.ExportCodebase;
import sorcer.util.junit.SorcerClient;
import sorcer.util.junit.SorcerRunner;
import sorcer.util.junit.SorcerService;

import java.util.List;

import static sorcer.eo.operator.*;

@RunWith(SorcerRunner.class)
//@Category(SorcerClient.class)
@ExportCodebase("org.sorcersoft.sorcer:ex0-api")
@SorcerService(":ex0-cfg")
public class HelloWorldTester {

    private static Logger logger = LoggerFactory.getLogger(HelloWorldTester.class);

    @Test
    public void getExertion() throws ExertionException, ContextException, SignatureException {
        System.setSecurityManager(new SecurityManager());
        logger.info("Starting HelloWorldTester");

        Task t1 = task("hello", sig("sayHelloWorld", HelloWorld.class),
                context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);

        logger.info("Got result: {}", get(out, "out/value"));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: {}", trace(out));
        List<ThrowableTrace> exceptions = out.getExceptions();
        if (!exceptions.isEmpty())
            logger.error("Exceptions: {}", exceptions);
        Assert.assertTrue("Exceptions!", exceptions.isEmpty());
    }
}
