package sorcer.eo;

import sorcer.service.*;

/**
* @author Rafał Krupiński
*/
public class TaskBuilder {
    protected Task task;

    public TaskBuilder(Task task) {
        this.task = task;
    }

    public SigBuilder sig(String selector, Class type) throws SignatureException {
        SigBuilder sig = new SigBuilder(SignatureFactory.sig(selector, type, null));
        task.addSignature(sig.get());
        return sig;
    }

    public TaskBuilder with(Signature sig){
        task.addSignature(sig);
        return this;
    }

    public TaskBuilder with(SigBuilder sigBuilder){
        task.addSignature(sigBuilder.get());
        return this;
    }

    public TaskBuilder with(Context ctx){
        task.setContext(ctx);
        return this;
    }

    public TaskBuilder with(CtxBuilder ctx){
        return with(ctx.get());
    }

    public Task get() {
        return task;
    }
}
