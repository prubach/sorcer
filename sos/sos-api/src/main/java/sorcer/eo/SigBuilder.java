package sorcer.eo;

import sorcer.service.Direction;
import sorcer.service.ReturnPath;
import sorcer.service.Signature;

/**
* @author Rafał Krupiński
*/
public class SigBuilder implements javax.inject.Provider<Signature> {
    protected Signature sig;

    public SigBuilder(Signature sig) {
        this.sig = sig;
    }

    public SigBuilder with(ReturnPath returnPath) {
        sig.setReturnPath(returnPath);
        return this;
    }

    public SigBuilder returning(String path, Direction dir){
        return with(new ReturnPath(path, dir));
    }

    public SigBuilder returning(String path, String... paths) {
        return with(new ReturnPath(path, paths));
    }

    public SigBuilder with(Signature.Type type){
        sig.setType(type);
        return this;
    }

    public Signature get() {
        return sig;
    }
}
