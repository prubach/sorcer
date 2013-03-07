/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.falcon.validation.context;

import sorcer.core.SorcerConstants;
import sorcer.core.context.ServiceContext;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;

/**
 * Example of a class with user defined api methods for the ServiceContext.
 * Simply subclass from the ServiceContextImpl and set the path as memeber
 * with the appropriate methods for set and get.
 * 
 * @author Michael Alger
 */
public class CalcContext extends ServiceContext implements SorcerConstants {

	private static final long serialVersionUID = 0L;
	protected static final String IN = "in" + CPS + "value" + CPS;
    protected static final String OUT = "out" + CPS + "value" + CPS;
    protected static final String A_VALUE = IN + "A";
    protected static final String B_VALUE = IN + "B";
    protected static final String N_VALUE = IN + "N";
    protected static final String X_VALUE = IN + "X";
    protected static final String H_VALUE = IN + "H";
    protected static final String ITER_VALUE = IN + "ITER";
    protected static final String FN = IN + "function";
    protected static final String SCALAR_FN_RESULT_XH = OUT + "functionResultXH";
    protected static final String SCALAR_FN_RESULT_X = OUT + "functionResultX";
    protected static final String DERIVATIVE_FN_RESULT = OUT + "derivativeResult";
    protected static final String OLD_DERIVATIVE_FN_RESULT = OUT + "oldDerivativeResult";
    protected static final String INTEGRAL_FN_RESULT = OUT + "integralResult";
    protected static final String OLD_INTEGRAL_FN_RESULT = OUT + "oldIntegralResult";
    
    /**
     * Default constructor
     */
    public CalcContext() {
        super();
    }
    
    /**
     * Overloaded constructor with context name parameter
     * @param name String
     */
    public CalcContext(String contextName) {
        super(contextName);
    }
    
    /**
     * Overloaded constructor with context name and root name parameters
     * @param contextName String
     * @param rootName String
     */
    public CalcContext(String contextName, String rootName) {
        super(contextName, rootName);
    }
    
    /**
     * Sets the A value into the context for integration
     * @param value double
     * @throws ContextException
     */
    public void setAValue(double value) throws ContextException {
    	this.putValue(A_VALUE, value);
    }
    
    /**
     * Returns the A value for integration
     * @return double
     * @throws ContextException
     */
    public double getAValue() throws ContextException {
    	return (Double) this.getValue(A_VALUE);
    }
    
    /**
     * Returns the path of the A variable for integration
     * @return String
     * @throws ContextException
     */
    public static String getAPath() throws ContextException {
    	return A_VALUE;
    }
    
    /**
     * Sets the B value into the context for integration
     * @param value double
     * @throws ContextException
     */
    public void setBValue(double value) throws ContextException {
    	this.putValue(B_VALUE, value);
    }
    
    /**
     * Returns the B value for integration
     * @return double
     * @throws ContextException
     */
    public double getBValue() throws ContextException {
    	return (Double) this.getValue(B_VALUE);
    }
    
    /**
     * Returns the path of the B variable for integration
     * @return String
     * @throws ContextException
     */
    public static String getBPath() throws ContextException {
    	return B_VALUE;
    }
    
    /**
     * Sets the N value into the context for integration
     * @param value double
     * @throws ContextException
     */
    public void setNValue(double value) throws ContextException {
    	this.putValue(N_VALUE, value);
    }
    
    /**
     * Returns the N value for integration
     * @return double
     * @throws ContextException
     */
    public double getNValue() throws ContextException {
    	return (Double) this.getValue(N_VALUE);
    }
    
    /**
     * Returns the path of the N variable for integration
     * @return String
     * @throws ContextException
     */
    public static String getNPath() throws ContextException {
    	return N_VALUE;
    }
    
    /**
     * Sets the x value into the context
     * @param xValue double
     * @throws ContextException
     */
    public void setXValue(double xValue) throws ContextException {
    	this.putValue(X_VALUE, xValue);
    }
    
    /**
     * Returns the x value
     * @return double
     * @throws ContextException
     */
    public double getXValue() throws ContextException {
    	return (Double) this.getValue(X_VALUE);
    }
    
    /**
     * Returns the path of the X variable
     * @return String
     * @throws ContextException
     */
    public static String getXPath() throws ContextException {
    	return X_VALUE;
    }
    
    /**
     * Sets the hValue into the context
     * @param hValue double
     * @throws ContextException
     */
    public void setHValue(double hValue) throws ContextException {
    	this.putValue(H_VALUE, hValue);
    }
    
    /**
     * Returns the h value
     * @return double
     * @throws ContextException
     */
    public double getHValue() throws ContextException {
    	return (Double) this.getValue(H_VALUE);
    }
    
    /**
     * Returns the path/key of the h variable
     * @return String
     * @throws ContextException
     */
    public static String getHPath() {
    	return H_VALUE;
    }
    
    /**
     * Sets the value of the current iteration
     * @param count long type the number of iteration
     * @throws ContextException
     */
    public void setIterValue(double count) throws ContextException {
    	this.putValue(ITER_VALUE, count);
    }
    
    /**
     * Returns the iteration count
     * @return long
     * @throws ContextException
     */
    public double getIterValue() throws ContextException {
    	return (Double) this.getValue(ITER_VALUE);
    }
    
    /**
     * Returns the path of the iteration data node
     * @return String
     * @throws ContextException
     */
    public static String getIterPath() throws EvaluationException {
    	return ITER_VALUE;
    }
  
    /**
     * Sets the Function object into the context
     * @param func Object of type Function
     * @throws ContextException
     */
    public void setFunction(Object func) throws ContextException {
		this.putValue(FN, func);
    }
    
    /**
     * Returns the Function object
     * @return Object of type Function
     * @throws ContextException
     */
    public Object getFunction() throws ContextException {
    	return this.getValue(FN);
    }
    
    /**
     * Sets the scalar result of the funtion into the context
     * @param result double
     * @throws ContextException
     */
    public void setScalarFnResultXH(double result) throws ContextException {
    	this.putValue(SCALAR_FN_RESULT_XH, result);
    }

    /**
     * Returns the scalar result of the function using X + H
     * @return Double
     * @throws ContextException
     */
    public double getScalarFnResultXH() throws ContextException {
    	return (Double) this.getValue(SCALAR_FN_RESULT_XH);
    }
    
    /**
     * Sets the scalar result of the funtion into the context
     * @param result double
     * @throws ContextException
     */
    public void setScalarFnResultX(double result) throws ContextException {
    	this.putValue(SCALAR_FN_RESULT_X, result);
    }

    /**
     * Returns the scalar result of the function for X value
     * @return Double
     * @throws ContextException
     */
    public double getScalarFnResultX() throws ContextException {
    	return (Double) this.getValue(SCALAR_FN_RESULT_X);
    }
    
    /**
     * Sets the derivative result into the context
     * @param result double
     * @throws ContextException
     */
    public void setDerivativeFnResult(double result) throws ContextException {
    	this.putValue(DERIVATIVE_FN_RESULT, result);
    }

    /**
     * Returns the derivative result
     * @return Double
     * @throws ContextException
     */
    public double getDerivativeFnResult() throws ContextException {
    	return (Double) this.getValue(DERIVATIVE_FN_RESULT);
    }
    
    /**
     * Returns the path of the derivative result
     * @return String
     * @throws ContextException
     */
    public static String getDerivativeFnPath() throws ContextException {
    	return DERIVATIVE_FN_RESULT;
    }
    
    /**
     * Sets the old derivative result value
     * @param oldResult double
     * @throws ContextException
     */
    public void setOldDerivativeFnResult(double oldResult) throws ContextException {
    	this.putValue(OLD_DERIVATIVE_FN_RESULT, oldResult);
    }
    
    /**
     * Returns the value of the previous derivative value
     * @return double
     * @throws ContextException
     */
    public double getOldDerivativeFnResult() throws ContextException {
    	return (Double) this.getValue(OLD_DERIVATIVE_FN_RESULT);
    }
    
    /**
     * Returns the path of hte previous derivative value
     * @return String
     * @throws ContextException
     */
    public static String getOldDerivativeFnPath() throws ContextException {
    	return OLD_DERIVATIVE_FN_RESULT;
    }
    
    /**
     * Sets the integral result into the context
     * @param result double
     * @throws ContextException
     */
    public void setIntegralFnResult(double result) throws ContextException {
    	this.putValue(INTEGRAL_FN_RESULT, result);
    }

    /**
     * Returns the integral result
     * @return Double
     * @throws ContextException
     */
    public double getIntegralFnResult() throws ContextException {
    	return (Double) this.getValue(INTEGRAL_FN_RESULT);
    }
    
    /**
     * Returns the path of the integral result
     * @return String
     * @throws ContextException
     */
    public static String getIntegralFnPath() throws ContextException {
    	return INTEGRAL_FN_RESULT;
    }
    
    /**
     * Sets the integral result into the context
     * @param result double
     * @throws ContextException
     */
    public void setOldIntegralFnResult(double result) throws ContextException {
    	this.putValue(OLD_INTEGRAL_FN_RESULT, result);
    }

    /**
     * Returns the integral result
     * @return Double
     * @throws ContextException
     */
    public double getOldIntegralFnResult() throws ContextException {
    	return (Double) this.getValue(OLD_INTEGRAL_FN_RESULT);
    }
    
    /**
     * Returns the path of the integral result
     * @return String
     * @throws ContextException
     */
    public static String getOldIntegralFnPath() throws ContextException {
    	return OLD_INTEGRAL_FN_RESULT;
    }
    
}
