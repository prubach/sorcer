package sorcer.core.loki.exertion;

import sorcer.core.SorcerConstants;
import sorcer.core.context.ServiceContext;
import sorcer.service.ContextException;

/**
 * SimpleContext is a extension of ServiceContext designed for the use of the "Simple Provider" and
 * its requestors. This class provides mutators and accessors methods to simplify the key and value
 * relationship, thus making it transparent to both provider and requestor.
 * 
 * @author Michael Alger
 */
public class SimpleContext extends ServiceContext implements SorcerConstants {

    final private String IN = rootName + CPS + IN_VALUE + CPS;
    final private String OUT = rootName + CPS + OUT_VALUE + CPS;
    final private String REQ_MSG = IN + "requestorMsg";
    final private String REQ_HOST = IN + "requestorHost";
    final private String PRV_MSG = OUT + "providerMsg";
    final private String PRV_HOST = OUT + "providerHost";
    final private String PRV_NAME = OUT + "providerName";
    final private String PRV_GREETING = OUT + "prvGreeting";
    final private String SENTINEL_VAR = OUT + "sentinel";
    final private String COUNT_VAR = OUT + "counter";
    

    /**
     * Default constructor
     */
    public SimpleContext() {
        super();
    }

    /**
     * Constructor
     * 
     * @param name The name of the ServiceContext as well as the root name
     */
    public SimpleContext(String name) {
        super(name);
    }

    /**
     * Sets the requestor's message into the context for the provider
     * 
     * @param message The string that the "Simple Provider" will print
     * @throws ContextException
     */
    public void setReqMessage(String message) throws ContextException {
        this.putInValue(REQ_MSG, message);
    }

    /**
     * Sets the requestor's host name into the context
     * 
     * @param host Host name of the requestor
     * @throws ContextException
     */
    public void setReqHost(String host) throws ContextException {
        this.putInValue(REQ_HOST, host);
    }

    /**
     * Sets the provider's name into the context
     * 
     * @param prvName The name of the provider which executed the exertion
     * @throws ContextException
     */
    public void setPrvName(String prvName) throws ContextException {
        this.putOutValue(PRV_NAME, prvName);
    }

    /**
     * Sets the provider's host into the context
     * 
     * @param prvHost The host name of the provider which executed the exertion
     * @throws ContextException
     */
    public void setPrvHost(String prvHost) throws ContextException {
        this.putOutValue(PRV_HOST, prvHost);
    }

    /**
     * Sets the provider's greeting to the requestor
     * 
     * @param greeting The random greeting of the "Simple Provider"
     * @throws ContextException
     */
    public void setPrvGreeting(String greeting) throws ContextException {
        this.putOutValue(PRV_GREETING, greeting);
    }

    /**
     * Sets the provider's message for the client
     * 
     * @param message a String object
     * @throws ContextException
     */
    public void setPrvMessage(String message) throws ContextException {
        this.putOutValue(PRV_MSG, message);
    }

    /**
     * Returns the requestor's message for the provider
     * 
     * @return String
     * @throws ContextException
     */
    public String getReqMessage() throws ContextException {
        return (String) this.get(REQ_MSG);
    }

    /**
     * Returns the requestor's host name
     * 
     * @return String
     * @throws ContextException
     */
    public String getReqHost() throws ContextException {
        return (String) this.get(REQ_HOST);
    }

    /**
     * Returns the provider's name which executed the exertion
     * 
     * @return String
     * @throws ContextException
     */
    public String getPrvName() throws ContextException {
        return (String) this.get(PRV_NAME);
    }

    /**
     * Returns the provider's host which executed the exertion
     * 
     * @return String
     * @throws ContextException
     */
    public String getPrvHost() throws ContextException {
        return (String) this.get(PRV_HOST);
    }

    /**
     * Returns the provider's greeting
     * 
     * @return String
     * @throws ContextException
     */
    public String getPrvGreeting() throws ContextException {
        return (String) this.get(PRV_GREETING);
    }

    /**
     * Returns the provider's message
     * 
     * @return String
     * @throws ContextException
     */
    public String getPrvMessage() throws ContextException {
        return (String) this.get(PRV_MSG);
    }
    
    /**
     * Sets a data node for testing with condition expression
     * @param value the object stored in the data node
     * @throws ContextException
     * @WhileExertion
     */
    public void setSentinelVar(Object value) throws ContextException {
        this.putOutValue(SENTINEL_VAR, value);
    }
    
    /**
     * Returns the value of the data node
     * @return Object
     * @throws ContextException
     */
    public Object getSetinelVar() throws ContextException {
        return this.get(SENTINEL_VAR);
    }
    
    /**
     * Simply returns the path of the data node that holds the sentinel 
     * @return String
     */
    public String getSentinelVarPath() {
        return SENTINEL_VAR;
    }
    
    /**
     * Sets a data node for testing with condition expression
     * @param value the object stored in the data node
     * @throws ContextException
     * @WhileExertion
     */
    public void setCounterVar(Object value) throws ContextException {
        this.putOutValue(COUNT_VAR, value);
    }
    
    /**
     * Returns the value of the data node
     * @return Object
     * @throws ContextException
     */
    public Object getCounterVar() throws ContextException {
        return this.get(COUNT_VAR);
    }
    
    /**
     * Simply returns the path of the data node that holds the counter 
     * @return String
     */
    public String getCounterVarPath() {
        return COUNT_VAR;
    }
    
    /**
     * 
     */
    public String getPrvGreetingPath() {
        return PRV_GREETING;
    }
}

