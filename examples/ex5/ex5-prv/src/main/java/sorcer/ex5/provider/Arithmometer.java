/**
 *
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Sorcersoft.com S.A.
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
package sorcer.ex5.provider;

import static sorcer.eo.operator.revalue;
import static sorcer.eo.operator.path;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import sorcer.core.SorcerEnv;
import sorcer.core.context.ArrayContext;
import sorcer.core.context.Contexts;
import sorcer.core.context.PositionalContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.ReturnPath;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Arithmometer implements Serializable {
	
	public static final String ADD = "add";

	public static final String SUBTRACT = "subtract";

	public static final String MULTIPLY = "multiply";

	public static final String DIVIDE = "divide";

    public static final String AVERAGE = "average";

    public static final String RESULT_PATH = "result/value";
			
	public final static Logger logger = Logger.getLogger(Arithmometer.class
			.getName());

	/**
	 * Implements the {@link Adder} interface.
	 * 
	 * @param context
	 *            input context for this operation
	 * @return an output service context
	 * @throws java.rmi.RemoteException
	 * @throws ContextException 
	 */
	public Context add(Context context) throws RemoteException, ContextException {
		if (context instanceof ArrayContext) {
			return calculateFromArrayContext(context, ADD);
		} else {
			return calculateFromPositionalContext(context, ADD);
		}
	}

	/**
	 * Implements the {@link Subtractor} interface.
	 * 
	 * @param context
	 *            input context for this operation
	 * @return an output service context
	 * @throws java.rmi.RemoteException
	 * @throws ContextException 
	 */
	public Context subtract(Context context)
			throws RemoteException, ContextException {
		if (context instanceof ArrayContext) {
			return calculateFromArrayContext(context, SUBTRACT);
		} else {
			return calculateFromPositionalContext(context, SUBTRACT);
		}
	}

	/**
	 * Implements the {@link Multiplier} interface.
	 * 
	 * @param context
	 *            input context for this operation
	 * @return an output service context
	 * @throws java.rmi.RemoteException
	 * @throws ContextException 
	 */
	public Context multiply(Context context)
			throws RemoteException, ContextException {
		if (context instanceof ArrayContext) {
			return calculateFromArrayContext(context, MULTIPLY);
		} else {
			return calculateFromPositionalContext(context, MULTIPLY);
		}
	}

	/**
	 * Implements the {@link Divider} interface.
	 * 
	 * @param context
	 *            input context for this operation
	 * @return an output service context
	 * @throws ContextException 
	 * @throws RemoteException
	 */
	public Context divide(Context context) throws RemoteException, ContextException {
		if (context instanceof ArrayContext) {
			return calculateFromArrayContext(context, DIVIDE);
		} else {
			return calculateFromPositionalContext(context, DIVIDE);
		}
	}

    /**
     * Implements the {@link Divider} interface.
     *
     * @param context
     *            input context for this operation
     * @return an output service context
     * @throws ContextException
     * @throws RemoteException
     */
    public Context average(Context context) throws RemoteException, ContextException {
        if (context instanceof ArrayContext) {
            return calculateFromArrayContext(context, AVERAGE);
        } else {
            return calculateFromPositionalContext(context, AVERAGE);
        }
    }

	/**
	 * Calculates the result of arithmetic operation specified by a selector
	 * (add, subtract, multiply, or divide) from the instance of ArrayContext.
	 * 
	 * @param context
	 *            service context
	 * @param selector
	 *            a name of arithmetic operation
	 * @throws java.rmi.RemoteException
	 * @throws ContextException 
	 */
	private Context calculateFromArrayContext(Context context, String selector)
			throws RemoteException, ContextException {
		ArrayContext cxt = (ArrayContext) context;
		try {
			// get sorted list of input values
			List<Double> inputs = (List<Double>)cxt.getInValues();
			logger.info("inputs: \n" + inputs);
			List<String> outpaths = cxt.getOutPaths();
			logger.info("outpaths: \n" + outpaths);

			double result;
			if (selector.equals(ADD)) {
				result = 0;
				for (Double value : inputs)
					result += value;
			} else if (selector.equals(SUBTRACT)) {
				result = inputs.get(0);
				for (int i = 1; i < inputs.size(); i++)
					result -= inputs.get(i);
			} else if (selector.equals(MULTIPLY)) {
				result = inputs.get(0);
				for (int i = 1; i < inputs.size(); i++)
					result *= inputs.get(i);
			} else if (selector.equals(DIVIDE)) {
				result = inputs.get(0);
				for (int i = 1; i < inputs.size(); i++)
					result /= inputs.get(i);
			}  else if (selector.equals(AVERAGE)) {
                result = 0;
                for (Double value : inputs)
                    result += value;
                result = result/inputs.size();
            } else throw new ContextException("Not supported operation: " + selector);

			logger.info(selector + " result: \n" + result);

			String outputMessage = "calculated by " + getHostname();
			// set return value
			if (context.getReturnPath() != null) {
				context.setReturnValue(result);
			}
			//other ways to indicate the output value
			else if (outpaths.size() == 1) {
				// put the result in the existing output path
				cxt.putValue(outpaths.get(0), result);
				cxt.putValue(path(outpaths.get(0), ArrayContext.DESCRIPTION), outputMessage);
			} else {
				// put the result for a new output path
				logger.info("max index; " + cxt.getMaxIndex());
				int oi = cxt.getMaxIndex() + 1;
				cxt.ov(oi, result);
				cxt.ovd(oi, outputMessage);
			}

		} catch (Exception ex) {
			// ContextException, UnknownHostException
			context.reportException(ex);
			throw new ContextException(selector + " calculate exception", ex);
		}
		return context;
	}

	/**
	 * Calculates the result of arithmetic operation specified by a selector
	 * (add, subtract, multiply, or divide) from the instance of ServiceContext.
	 * 
	 * @param context
	 *            service context
	 * @param selector
	 *            a name of arithmetic operation
	 * @throws java.rmi.RemoteException
	 * @throws ContextException
	 */
	private Context calculateFromPositionalContext(Context context, String selector)
			throws RemoteException, ContextException {
		PositionalContext cxt = (PositionalContext) context;
		try {
			//logger.info("selector: " + ((ServiceContext)context).getCurrentSelector());
			// get sorted list of input values
			List<Double> inputs = (List<Double>)Contexts.getNamedInValues(context);
			if (inputs == null || inputs.size() == 0) {
				inputs = (List<Double>)Contexts.getPrefixedInValues(context);
				logger.info("prefixed inputs: \n" + inputs);
			}
			//logger.info("named inputs: \n" + inputs);
			if (inputs == null || inputs.size() == 0)
				inputs = (List<Double>)cxt.getInValues();
			//logger.info("inputs: \n" + inputs);
			List<String> outpaths = cxt.getOutPaths();
			//logger.info("outpaths: \n" + outpaths);

			double result;
			if (selector.equals(ADD)) {
					result = (Double)revalue(inputs.get(0));
				for (int i = 1; i < inputs.size(); i++)
					result += (Double)revalue(inputs.get(i));
			} else if (selector.equals(SUBTRACT)) {
				ReturnPath<?> rp = context.getReturnPath();
				if (rp != null && rp.argPaths != null && rp.argPaths.length > 0) {
					result = (Double) revalue(cxt.getValue(rp.argPaths[0]));
					result -= (Double) revalue(cxt.getValue(rp.argPaths[1]));
				} else {
					if (inputs.size() > 2) {
						throw new ContextException("more than two arguments for subtraction");
					}
					result = (Double) revalue(cxt.getInValueAt(1));
					result -= (Double) revalue(cxt.getInValueAt(2));
				}
			} else if (selector.equals(MULTIPLY)) {
				result = inputs.get(0);
				for (int i = 1; i < inputs.size(); i++)
					result *= (Double)revalue(inputs.get(i));
			} else if (selector.equals(DIVIDE)) {
				if (inputs.size() > 2)
					throw new ContextException("more than two arguments for division");
				result = (Double)revalue(cxt.getInValueAt(1));
				result /= (Double)revalue(cxt.getInValueAt(2));
			} else if (selector.equals(AVERAGE)) {
                result = (Double)revalue(inputs.get(0));
                for (int i = 1; i < inputs.size(); i++) {
                    result += (Double)revalue(inputs.get(i));
                }
                result = result/inputs.size();
            } else {
                throw new ContextException("Not supported operation: " +  selector);
            }

			logger.info(selector + " result: \n" + result);

			String outputMessage = "calculated by " + getHostname();
			if (context.getReturnPath() != null) {
				context.setReturnValue(result);
			}
			else if (outpaths.size() == 1) {
				// put the result in the existing output path
				cxt.putValue(outpaths.get(0), result);
				cxt.putValue(path(outpaths.get(0), ArrayContext.DESCRIPTION), outputMessage);
			} else {
				cxt.putValue(RESULT_PATH, result);
				cxt.putValue(path(RESULT_PATH, ArrayContext.DESCRIPTION), outputMessage);
			}
		} catch (Exception ex) {
			// ContextException, UnknownHostException
			ex.printStackTrace();
			context.reportException(ex);
			throw new ContextException(selector + " calculate exception", ex);
		}
		return context;
	}
	

	/**
	 * Returns name of the local host.
	 * 
	 * @return local host name
	 * @throws java.net.UnknownHostException
	 */
	private String getHostname() throws UnknownHostException {
		return SorcerEnv.getLocalHost().getHostName();
	}

}
