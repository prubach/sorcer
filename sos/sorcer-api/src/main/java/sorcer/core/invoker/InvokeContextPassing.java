package sorcer.core.invoker;


import sorcer.service.Context;

/**
 * This interface allows the passing of invoke context to classes from the sos-modeling framework
 * User: prubach
 * Date: 20.09.13
 */
public interface InvokeContextPassing {

    public void setInvokeContext(Context invokeContext);
}
