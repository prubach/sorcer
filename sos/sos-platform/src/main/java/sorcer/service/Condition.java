/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 SorcerSoft.org.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sorcer.service;

import groovy.lang.Closure;

import java.io.Serializable;
import java.rmi.RemoteException;


/**
 * A Condition specifies a conditional value in a given service context for its free variables
 * in the form of path/value pairs with paths being guards's parameters.
 *
 * @author Mike Sobolewski
 * @see Exertion
 * @see WhileExertion
 * @see IfExertion
 */
@SuppressWarnings("rawtypes")
public class Condition implements Evaluation<Object>, Conditional, Serializable {

    private static final long serialVersionUID = -7310117070480410642L;
    public static String CONDITION_VALUE = "condition/value";
    public static String CONDITION_TARGET = "condition/target";
    protected Context<?> conditionaContext;
    protected String evaluationPath;
    protected String closureExpression;
    protected String[] pars;
    Boolean status = null;
    private Closure closure;

    public Condition() {
        // do nothing
    }

    public Condition(Boolean status) {
        this.status = status;
    }

    public Condition(Context<?> context) {
        conditionaContext = context;
    }

    public Condition(Context<?> context, String parPath) {
        evaluationPath = parPath;
        conditionaContext = context;
    }

    public Condition(Context<?> context, String closure, String... parameters) {
        this.closureExpression = closure;
        conditionaContext = context;
        this.pars = parameters;
    }

    /**
     * The isTrue method is responsible for evaluating the underlying contextual
     * condition.
     *
     * @return boolean true or false depending on given contexts
     * @throws ExertionException if there is any problem within the isTrue method.
     * @throws ContextException
     */
    public boolean isTrue() throws ContextException {
        if (status != null)
            return status;

        Object obj = null;
        Object[] args = null;
        if (closure != null) {
            args = new Object[pars.length];
            for (int i = 0; i < pars.length; i++) {
                args[i] = conditionaContext.getValue(pars[i]);
            }
            obj = closure.call(args);
        } else if (evaluationPath != null && conditionaContext != null) {
            obj = conditionaContext.getValue(evaluationPath);
        } else if (closureExpression != null && conditionaContext != null) {

             /*conditionaContext.putValue("closure",
                    new Invoker((ParModel)conditionaContext));
            ((Invoker) conditionaContext.get("closure"))
                    .setEvaluator(groovy(closureExpression));


            closure = (Closure)conditionaContext.getValue("closure");
            args = new Object[pars.length];
            for (int i = 0; i < pars.length; i++) {
                args[i] = conditionaContext.getValue(pars[i]);
            }
            obj = closure.call(args);*/
        }

        if (obj instanceof Boolean)
            return (Boolean) obj;
        else if (obj != null)
            return true;
        else
            return false;
    }

    /* (non-Javadoc)
     * @see sorcer.service.Evaluation#asis()
	 */
    @Override
    public Object asis() throws EvaluationException, RemoteException {
        return getValue();
    }

    /* (non-Javadoc)
     * @see sorcer.service.Evaluation#getValue(sorcer.service.Parameter[])
     */
    @Override
    public Object getValue(Arg... entries) throws EvaluationException,
            RemoteException {
        try {
            return isTrue();
        } catch (ContextException e) {
            throw new EvaluationException(e);
        }
    }

    /* (non-Javadoc)
     * @see sorcer.service.Evaluation#substitute(sorcer.service.Parameter[])
     */
    @Override
    public Evaluation<Object> substitute(Arg... entries)
            throws EvaluationException, RemoteException {
        conditionaContext.substitute(entries);
        return this;
    }
}