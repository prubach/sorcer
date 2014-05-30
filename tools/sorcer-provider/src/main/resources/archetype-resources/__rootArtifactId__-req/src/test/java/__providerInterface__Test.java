package ${package};

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
@ExportCodebase({"${groupId}:${rootArtifactId}-dl:pom:${version}"})
@SorcerServiceConfiguration(":${rootArtifactId}-cfg")
public class ${providerInterface}Test {

    private static Logger logger = LoggerFactory.getLogger(${providerInterface}Test.class);

    @Test
    public void test${providerInterface}() throws ContextException, SignatureException, ExertionException {
        logger.info("Starting ${providerInterface}Test");

        Task t1 = task("hello", sig("sayHelloWorld", ${providerInterface}.class),
                context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);

        logger.info("Got result: {}", get(out, "out/value"));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: " + trace(out));
        assertEquals("Hello there - TESTER", get(out, "out/value"));
    }
}
