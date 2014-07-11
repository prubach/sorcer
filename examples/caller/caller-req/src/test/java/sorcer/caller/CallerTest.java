package sorcer.caller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.junit.*;
import sorcer.service.*;
import static org.junit.Assert.assertEquals;

import static sorcer.eo.operator.*;

@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:caller-dl:pom:1.0.0-SNAPSHOT"})
@SorcerServiceConfiguration(":caller-cfg")
public class CallerTest {

    private static Logger logger = LoggerFactory.getLogger(CallerTest.class);

    @Test
    public void testCaller() throws ContextException, SignatureException, ExertionException {
        logger.info("Starting CallerTest");

        Task t1 = task("hello", sig("sayHelloWorld", Caller.class),
                context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);

        logger.info("Got result: {}", get(out, "out/value"));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: " + trace(out));
        assertEquals("Hello there - TESTER", get(out, "out/value"));
    }
}
