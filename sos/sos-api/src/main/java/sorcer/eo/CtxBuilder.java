package sorcer.eo;

import sorcer.service.Context;
import sorcer.service.ContextException;

/**
* @author Rafał Krupiński
*/
public class CtxBuilder implements javax.inject.Provider<Context>{
    protected Context ctx;

    public CtxBuilder(Context ctx) {
        this.ctx = ctx;
    }

    public CtxBuilder in(String path, Object value) throws ContextException {
        ctx.putInValue(path, value);
        return this;
    }

    @Override
    public Context get() {
        return ctx;
    }

}
