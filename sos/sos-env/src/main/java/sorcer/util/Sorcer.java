package sorcer.util;

import sorcer.core.SorcerEnv;

import java.util.Map;

/**
 * For compatibility with older scripts
 * User: prubach
 * Date: 01.07.13
 * Time: 12:40
 */
public class Sorcer extends SorcerEnv {

    static {
        sorcerEnv = new Sorcer();
    }

    public Sorcer() {
        super();
    }
}
