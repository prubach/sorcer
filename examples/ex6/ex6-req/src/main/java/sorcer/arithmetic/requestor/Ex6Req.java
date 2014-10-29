package sorcer.arithmetic.requestor;

import sorcer.service.*;
import sorcer.core.requestor.ServiceRequestor;

import static sorcer.eo.operator.*;

/**
 * SORCER class
 * User: prubach
 * Date: 03.10.14
 */
public class Ex6Req {

    public static void main(String[] args) throws Exception {
       /* ServiceRequestor.prepareEnvironment();
        ServiceRequestor.prepareCodebase(new String[]{"org.sorcersoft.sorcer:ex6-api"});


        Task f4 = task("f4", sig("multiply", junit.sorcer.core.provider.Multiplier.class),
                context("multiply", input(path("arg/x1"), 10.0d), input(path("arg/x2"), 50.0d),
                        out(path("result/y1"), null)));

        Task f5 = task("f5", sig("add", junit.sorcer.core.provider.Adder.class),
                context("add", input(path("arg/x3"), 20.0d), input(path("arg/x4"), 80.0d),
                        output(path("result/y2"), null)));

        Task f3 = task("f3",
                sig("subtract", junit.sorcer.core.provider.Subtractor.class),
                context("subtract",
                        input(path("arg/x5"), null),
                        input(path("arg/x6"), null),
                        output(path("result/y3"), null)
                )
        );

        Job job = job("f1", job("f2", f4, f5), f3,
                pipe(out(f4, path("result/y1")), input(f3, path("arg/x5"))),
                pipe(out(f5, path("result/y2")), input(f3, path("arg/x6"))));
        Exertion result = job.exert();
        Context outctx = result.getContext();

        System.out.println("OUT Context: " + outctx);
        System.out.println("RESULT: " + outctx.getValue("f1/f3/result/y3"));*/
    }
}
