package junit.sorcer.core.provider;

import sorcer.service.Context;

import static sorcer.eo.operator.context;
import static sorcer.eo.operator.input;

/**
 * SORCER class
 * User: prubach
 * Date: 21.07.14
 */
public class AddContext {
    public static Context createContext() throws Exception {
        Context cxt = context("add", input("arg/x1", 20.0), input("arg/x2", 80.0));
        return  cxt;
    }
}
