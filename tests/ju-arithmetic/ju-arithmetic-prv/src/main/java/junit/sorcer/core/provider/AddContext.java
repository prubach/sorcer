package junit.sorcer.core.provider;

import sorcer.service.Context;

import static sorcer.co.operator.inEnt;
import static sorcer.eo.operator.context;
import static sorcer.eo.operator.input;

/**
 * SORCER class
 * User: prubach
 * Date: 21.07.14
 */
public class AddContext {
    public static Context createContext() throws Exception {
        Context cxt = context("add", inEnt("arg/x1", 20.0), inEnt("arg/x2", 80.0));
        return  cxt;
    }


}
