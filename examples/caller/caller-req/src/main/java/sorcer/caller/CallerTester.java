package sorcer.caller;

import static sorcer.eo.operator.context;
import static sorcer.eo.operator.exert;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.path;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.get;
import static sorcer.eo.operator.trace;
import static sorcer.eo.operator.task;

import java.rmi.RMISecurityManager;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.core.SorcerConstants;
import sorcer.service.*;

public class CallerTester extends ServiceRequestor{
    private static Logger logger = LoggerFactory.getLogger(CallerTester.class);

    public Exertion getExertion(String... args) throws ExertionException,
        ContextException, SignatureException {
        System.setSecurityManager(new RMISecurityManager());
        logger.info("Starting CallerTester");

        Context ctx = new PositionalContext("caller");
        String[] comms = new String[] { "java" };
        String[] argss = new String[] { "-version" };
        //String[] argss = new String[] { "-s", "*" };
        CallerUtil.setCmds(ctx, comms);
        CallerUtil.setArgs(ctx, argss);
        CallerUtil.setBin(ctx);
        CallerUtil.setWorkingDir(ctx, ".");

        Task t1 = task("test", sig("execute", Caller.class), ctx);

        logger.info("Task t1 prepared: " + t1);
        Exertion out = exert(t1);


        //logger.info("GOT: " + out.getContext());
        logger.info("Got result: " + CallerUtil.getCallOutput(ctx));
        logger.info("----------------------------------------------------------------");
        logger.info("Task t1 trace: {}" + trace(out));
        return out;
    }
}
