package com.example.sorcer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.tools.webster.InternalWebster;
import sorcer.tools.webster.Webster;

import java.rmi.RMISecurityManager;

import static sorcer.eo.operator.*;

public class SampleServiceTester{

	private static Logger logger = LoggerFactory.getLogger(SampleServiceTester.class);

    public static void main(String[]args) throws ExertionException, SignatureException, ContextException {
        Webster webster = InternalWebster.startRequestorWebsterFromProperties();
        try {
            new SampleServiceTester().getExertion();
        }finally {
            webster.terminate();
        }
    }

	public Exertion getExertion(String... args) throws ExertionException,
                ContextException, SignatureException {
		System.setSecurityManager(new RMISecurityManager());
		logger.info("Starting SampleServiceTester");

		Task t1 = task("hello", sig("sayHelloWorld", SampleService.class),
				context("Hello", in(path("in", "value"), "TESTER"), out(path("out", "value"), null)));

		logger.info("Task t1 prepared: " + t1);
		Exertion out = exert(t1);

		logger.info("Got result: " + get(out, "out/value"));
		logger.info("----------------------------------------------------------------");
		logger.info("Task t1 trace: " + trace(out));
        return out;
    }
}
