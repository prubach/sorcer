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

package sorcer.falcon.core.exertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.nfunk.jep.JEP;

import sorcer.core.SorcerConstants;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.falcon.base.Conditional;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Signature;
import sorcer.util.Log;
import sorcer.core.context.ControlContext.ThrowableTrace;

/**
 * WhileExertion is a new exertion extending from the {@link ServiceExertion}
 * and also implements the {@link Conditional} interface . This composite
 * exertion is compose of a condition and an Exertion {@link Exertion}. The
 * condition is the sentinel for the main loop, it can be set by implementing
 * the Conditional interface or for legacy provider by explicitly setting the
 * attributes of the WhileExertion. The inner exertion can by any type of
 * Exertion, thus allowing us to create a recursive control structure comprise
 * of Conditional, jobs, and tasks.
 * 
 * @author Michael Alger
 * @see Exertion
 * @see Conditional
 * @see IfExertion
 * @see WhileExertion
 */
public class WhileExertion extends ServiceExertion implements Conditional {

	private static final long serialVersionUID = -6834263911252274917L;

	/**
	 * This is the base Exertion which the WhileExertion will iterate.
	 */
	public Exertion baseExertion;

	/**
	 * This is the boolean expression for the legacy usage, when the condition
	 * component is not given.
	 */
	protected String expression;

	/**
	 * This is the map reference between the user defined variables to the data
	 * node path for legacy usage, when the condition component is not given.
	 */
	protected Map<String, Object> mapReference = new HashMap<String, Object>();

	/**
	 * This is the reference to the condition component, when the condition is
	 * given.
	 */
	protected Conditional condition;

	/**
	 * Logger for the provider.
	 */
	protected final transient static Logger testLog = Log.getTestLog(); // logger

	/**
	 * Defualt constructor.
	 */
	public WhileExertion() {
		super();
	}

	/**
	 * Overloaded constructor with a Exertion parameter for the baseExertion.
	 * 
	 * @param baseExertion
	 *            An Exertion which can be a job, task, or Conditional
	 * @see NetTask
	 * @see NetJob
	 * @see Conditional
	 */
	public WhileExertion(Exertion baseExertion) {
		this.baseExertion = baseExertion;
	}

	/**
	 * Overloaded constructor which takes in a Conditional condition component
	 * and an Exertion.
	 * 
	 * @param baseExertion
	 *            An Exertion which can be a job, task, or Conditional
	 * @param condition
	 *            The condition component which must implement the Conditional
	 *            interface
	 */
	public WhileExertion(Conditional condition, Exertion baseExertion) {
		this.condition = condition;
		this.baseExertion = baseExertion;
	}

	/**
	 * Returns the condition component, which is an object implementing the
	 * {@link Condtional} interface for this looping exertion.
	 * 
	 * @return a condition for this exertion
	 */
	public Conditional getCondition() {
		return condition;
	}

	/**
	 * Returns the baseExertion of the WhileExertion container.
	 * 
	 * @return Exertion the base Exertion inside the WhileExertion
	 */
	public Exertion getDoExertion() {
		return baseExertion;
	}

	/**
	 * Sets the modified Exertion to the baseExeriton of this WhileExertion.
	 * 
	 * @param exertion
	 *            Exertion
	 */
	public void setDoExertion(Exertion exertion) {
		if (this.isJob()) {
			this.baseExertion = exertion;
			// this.setDataContext(exertion.getDataContext()); doesn't work for job and
			// not needed for task
		}

		if (condition != null) {
			try {
				if (((ServiceExertion) exertion).isTask())
					condition.setConditionalContext(exertion.getDataContext());

				else if (((ServiceExertion) exertion).isJob())
					condition.setConditionalContext(((Job) exertion)
							.getMasterExertion().getDataContext());

			} catch (ContextException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the dataContext of the base exertion (job/task) recursively.
	 * 
	 * @param context
	 *            the modified dataContext
	 * @see Context
	 */
	public void setConditionalContext(Context context) {
		((ServiceExertion) baseExertion).setContext(context);
	}

	/**
	 * Add an exception to the actual job/task exertion.
	 * 
	 * @return boolean true or false depending on the success of the addExeption
	 *         operation
	 * @param e
	 *            the exception
	 * @see sorcer.service.ServiceExertion#reportException(java.lang.Exception)
	 */
	public void reportException(Exception e) {
		((ServiceExertion) baseExertion).reportException(e);
	}

	/**
	 * Returns the dataContext name of the actual job/task.
	 * 
	 * @return String the name of the dataContext
	 * @see sorcer.service.ServiceExertion#getContextName()
	 */
	public String getContextName() {
		return ((ServiceExertion) baseExertion).getDataContext().getName();
	}

	/**
	 * Returns the dataContext of a job or task.
	 * 
	 * @return ServiceContext
	 */
	public Context getDataContext() {
		if (this.isTask()) {
			return baseExertion.getDataContext();
		}

		else if (this.isJob()) {
			return ((Job) baseExertion).getMasterExertion().getDataContext();
		}

		else
			return null;
	}

	/**
	 * Returns true if the exertion is a job, otherwise returns false.
	 * 
	 * @return boolean value
	 * @see sorcer.service.ServiceExertion#isJob()
	 */
	public boolean isJob() {
		return ((ServiceExertion) baseExertion).isJob();
	}

	/**
	 * Returns true if the exertion is a task, otherwise returns false.
	 * 
	 * @return boolean
	 * @see sorcer.service.ServiceExertion#isTask()
	 */
	public boolean isTask() {
		return ((ServiceExertion) baseExertion).isTask();
	}

	/**
	 * Sets the Owner ID to the actual job/task exertion.
	 * 
	 * @param id
	 *            The unique id
	 * @see sorcer.service.ServiceExertion#setOwnerId(java.lang.String)
	 */
	public void setOwnerId(String id) {
		((ServiceExertion) baseExertion).setOwnerId(id);
	}

	/**
	 * Returns the task.
	 * 
	 * @return Exertion a ServiceTask
	 * @see sorcer.service.Task
	 */
	public Exertion task() {
		return baseExertion;
	}

	/**
	 * Returns the ServiceMethod of the job/task.
	 * 
	 * @return Signature of the Exertion
	 */
	public Signature getProcessSignature() {
		return baseExertion.getProcessSignature();
	}

	/**
	 * Sets a reference between the given variable name and dataContext path. This
	 * method is used for the legacy way when the condition component is not
	 * given.
	 * 
	 * @param variableName
	 *            Name of the variable for the expression
	 * @param contextPath
	 *            Context path for a given variable name
	 */
	public void setConditionVariable(String variableName, String contextPath) {
		mapReference.put(variableName, contextPath);
	}

	/**
	 * Sets the boolean expression to be evaluated by the WhileExertion for
	 * condition. the variables in the expression is set by using.
	 * 
	 * @param expression
	 *            the boolean expression
	 * @see {@link WhileExertion#setConditionVariable(String, String)}
	 */
	public void setCondition(String expression) {
		this.expression = expression;
	}

	/**
	 * Sets which variable will be incremented by the passed value after each
	 * iteration until the condition is satisfied.
	 * 
	 * @param variableName
	 *            String
	 * @param value
	 *            a Double object
	 */
	public void setConditionVariableIncrement(String variableName, Double value) {
		mapReference.put(SorcerConstants.C_INCREMENT + variableName, value);
	}

	/**
	 * Sets which variable will be decremented by the assigned value after each
	 * iteration until the condition is satisfied.
	 * 
	 * @param variableName
	 *            name of the variable
	 * @param value
	 *            the value which the variable will be decremented
	 */
	public void setConditionVariableDecrement(String variableName, Double value) {
		mapReference.put(SorcerConstants.C_DECREMENT + variableName, value);
	}

	/**
	 * This method increments or decrements the variables per iteration as part
	 * of a loop.
	 * 
	 * @throws ExertionException
	 *             if there is any problem with adjustConditionVariables
	 */
	public void adjustConditionVariables() throws ExertionException {
		testLog.finest("***Adjusting Condition Variables***");
		Context context = this.getDataContext();
		Map map = null;

		if (condition != null) {
			map = condition.getReferencingMap();
		} else if (expression != null) {
			map = mapReference;
		} else {
			throw new ExertionException("missing condition in "
					+ WhileExertion.class.getClass().getName());
		}

		Iterator iter = map.entrySet().iterator();

		int index = -1;
		double adjustment = 0;
		double value = 0;
		String path = null;
		String varName = null;

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			path = (String) entry.getKey();
			testLog.finest("adjustment path: " + path);

			if ((index = path.indexOf(SorcerConstants.C_INCREMENT)) != 0)
				index = path.indexOf(SorcerConstants.C_DECREMENT);

			if (index == 0) {
				adjustment = ((Double) entry.getValue()).doubleValue();
				varName = path.substring(path.lastIndexOf("/") + 1);

				try {
					value = ((Double) context.getValue((String) map
							.get(varName))).doubleValue();
					value += adjustment;
					context.putValue((String) map.get(varName), value);

					// need to increment the variables on the thenExertion and
					// elseExertion for consistency
					// note that contextlink wont work because the initial path
					// set on the contextLink is static
					// other data nodes may not begin with the initial path.
					if (baseExertion instanceof IfExertion) {
						((Context) context.getValue("in/dataContext/thenExertion"))
								.putValue((String) map.get(varName), value);
						((Context) context.getValue("in/dataContext/elseExertion"))
								.putValue((String) map.get(varName), value);
					}

				} catch (ContextException e) {
					testLog.finest("Context Exception in adjustVariable" + e);
					e.printStackTrace();
				}

				testLog.finest("***Adjusting Condition: variable: " + varName
						+ "\tpath: " + path + "\tvalue: " + value);
			}
		}
	}

	/**
	 * Evaluates the string expression from the given variables that correlates
	 * to a path, which then points to the actual data nodes in the dataContext.
	 * 
	 * @return Object result from the expression
	 */
	protected double evalCondition() {
		JEP jepParser = new JEP();
		Context context = this.getDataContext();
		Iterator iter = mapReference.entrySet().iterator();
		double result = 0;

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String varName = (String) entry.getKey();
			if (!varName.startsWith("in/") && (varName.indexOf("/") == -1)) {
				String path = (String) entry.getValue();
				Object value = null;
				try {
					value = context.getValue(path);
				} catch (ContextException e) {
					testLog.finest("evalCondition ContextException " + e);
					e.printStackTrace();
				}

				testLog.finest("***Condition Variable: " + varName
						+ "  \tpath: " + path + "  \tvalue: " + value);
				jepParser.addVariable(varName, value);
			}
		}

		testLog.finest("Evaluating Condition: (" + expression + ")");
		jepParser.parseExpression(expression);
		result = jepParser.getValue();
		jepParser = null;
		return result;
	}

	/**
	 * Returns true if the given condition is true.
	 * 
	 * @return boolean boolean value
	 * @throws ExertionException
	 *             any remote exception
	 */
	public boolean isTrue() throws ExertionException {
		if (condition != null) {
			try {
				condition.setConditionalContext(this.getDataContext());
			} catch (ContextException ce) {
				testLog.finest("Unable to setDataContext on Condition: " + ce);
				ce.printStackTrace();
			}
			boolean conditionResult = condition.isTrue();
			testLog.finest("isTrue(): " + conditionResult);
			return conditionResult;
		}

		else if (expression != null) {
			double result = evalCondition();

			if (result == 0 || result == 1.0) {
				testLog.finest("isTrue(): " + (result == 1.0));
				return (result == 1.0);
			} else {
				throw new ExertionException(
						"Boolean expression is not a valid condition "
								+ WhileExertion.class.getClass().getName());
			}
		}

		else {
			throw new ExertionException("missing condition in "
					+ WhileExertion.class.getClass().getName());
		}
	}

	/**
	 * Returns the Map reference of variable names and their associated dataContext
	 * path.
	 * 
	 * @return Map the map reference
	 * @see Map
	 */
	public Map<String, Object> getReferencingMap() {
		if (condition != null) {
			return condition.getReferencingMap();
		} else if (expression != null) {
			return mapReference;
		} else {
			return null;
		}
	}

	/**
	 * Returns the boolean expression.
	 * 
	 * @return String
	 */
	public String getExpression() {
		if (condition != null) {
			return condition.getExpression();
		} else if (expression != null) {
			return expression;
		} else {
			return null;
		}
	}

	/**
	 * Returns the has been executed by the IfExertion by checking the getStatus
	 * of these Exertion.
	 * 
	 * @return Exertion
	 */
	public Exertion getResult() {
		if (baseExertion instanceof Conditional) {
			if (baseExertion instanceof IfExertion) {
				return ((IfExertion) baseExertion).getResult();
			} else if (baseExertion instanceof WhileExertion) {
				return ((WhileExertion) baseExertion).getResult();
			} else {
				return null;
			}
		} else if (baseExertion instanceof ServiceExertion) {
			return baseExertion;
		} else if (baseExertion instanceof Job) {
			return ((Job) baseExertion).getMasterExertion();
		} else {
			return null;
		}
	}

	/**
	 * Return true if this <code>WhileExertion</code> is a tree.
	 * 
	 * @param visited
	 *            a set of visited exertions
	 * @return true if this <code>WhileExertion</code> is a tree
	 * @see Exertion#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		if (visited.contains(baseExertion)
				|| !((ServiceExertion) baseExertion).isTree(visited)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a <code>WhileExertion</code> in the specified format. Some
	 * exertions can be defined for thin clients that do not use RMI or Jini.
	 * 
	 * @param type
	 *            the type of needed exertion format
	 * @return
	 */
	public Exertion getUpdatedExertion(int type) {
		// TODO
		return this;
	}

	public boolean isQos() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.ServiceExertion#linkContext(sorcer.service.Context,
	 * java.lang.String)
	 */
	@Override
	public Context linkContext(Context context, String path)
			throws ContextException {
		throw new ContextException("linkContext operation not supported");
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExceptions()
	 */
	public List<ThrowableTrace> getThrowables() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#linkControlContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkControlContext(Context context, String path)
			throws ContextException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExertions()
	 */
	@Override
	public List<Exertion> getExertions() {
		List<Exertion> list = new ArrayList<Exertion>();
		list.add(baseExertion);
		return list;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#getExertions(java.util.List)
	 */
	@Override
	public List<Exertion> getExertions(List<Exertion> exs) {
		exs.add(baseExertion);
		return exs;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#addExertion(sorcer.service.Exertion)
	 */
	@Override
	public Exertion addExertion(Exertion component) {
		throw new RuntimeException("Tasks do not contain appended exertions!");
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#getValue(java.lang.String)
	 */
	@Override
	public Object getValue(String path) throws ContextException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#putValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object putValue(String path, Object value) throws ContextException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
