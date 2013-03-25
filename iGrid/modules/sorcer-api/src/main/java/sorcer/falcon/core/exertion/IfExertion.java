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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.nfunk.jep.JEP;

import sorcer.core.context.ServiceContext;
import sorcer.core.context.ThrowableTrace;
import sorcer.core.signature.NetSignature;
import sorcer.falcon.base.Conditional;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.ServiceExertion;
import sorcer.service.Signature;
import sorcer.util.Log;

/**
 * The IfExertion implements the Conditional interface. It supports the
 * branching algorithmic logic for the new exertions. It contains three
 * components, the condition component, thenExertion and the elseExertion
 * according to the if-else structure.
 * 
 * @author Michael Alger
 */
public class IfExertion extends ServiceExertion implements Conditional {

	private static final long serialVersionUID = 172930501527871L;

	/**
	 * The then exertion component.
	 */
	protected Exertion thenExertion;

	/**
	 * The else exertion component.
	 */
	protected Exertion elseExertion;

	/**
	 * The context of the ifExertion, independent from the component exertion's
	 * context.
	 */
	protected Context ifContext;

	/**
	 * The boolean expression string for legacy usage, using without the given
	 * condition component.
	 */
	protected String expression;

	/**
	 * The map reference between the variable name and data node path.
	 */
	protected Map<String, Object> mapReference = new HashMap<String, Object>();

	/**
	 * The condition component, if set the legacy attributes will not be used.
	 */
	private Conditional condition;

	/**
	 * Logger for the provider logs.
	 */
	protected final transient static Logger testLog = Log.getTestLog(); // logger

	/**
	 * Default Constructor.
	 */
	public IfExertion() {
		super();
		ifContext = new ServiceContext("IfContext");
		try {
			ifContext.setAttribute("exertionDone"); // currently not used
			ifContext.putValue("exertion/done", ServiceContext.EMPTY_LEAF);
		} catch (ContextException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Overloaded constructor taking in two exertions implementing the {@see
	 * Exertion} interface. The first arguement is for the then exertion and the
	 * other as the else exertion.
	 * 
	 * @param thenExertion
	 *            the then exertion component
	 * @param elseExertion
	 *            the else exertion component
	 */
	public IfExertion(Exertion thenExertion, Exertion elseExertion) {
		this();
		this.thenExertion = thenExertion;
		this.elseExertion = elseExertion;

		try {
			ifContext.putValue("in/context/thenExertion", thenExertion
					.getContext());
			ifContext.putValue("in/context/elseExertion", elseExertion
					.getContext());

			// initially set the ifContext with the data nodes of the
			// thenExertion context (mirror)
			// it is done this way to accomidate WhileExertion(IfExertion)
			// compound statments, where the
			// getContext of IfExertion which WhileExertion will use has
			// reference to both thenExertion context and
			// elseExertion context. This is needed to increment both data nodes
			// in the then and else context.
			Enumeration e = thenExertion.getContext().contextPaths();
			String path = null;

			while (e.hasMoreElements()) {
				path = new String((String) e.nextElement());
				ifContext.putValue(path, thenExertion.getContext().getValue(
						path));
			}
		} catch (ContextException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Overloaded constructor which takes two exertions and an object
	 * implementing the {@see Conditional} interface. This conditional object
	 * contains the condition for this IfExertion.
	 * 
	 * @param condition
	 *            the condition component
	 * @param thenExertion
	 *            the then component
	 * @param elseExertion
	 *            the else component
	 */
	public IfExertion(Conditional condition, Exertion thenExertion,
			Exertion elseExertion) {
		this(thenExertion, elseExertion);
		this.condition = condition;
	}

	/**
	 * Returns the then exertion component, can be a Conditional, job or task.
	 * 
	 * @return Exertion
	 */
	public Exertion getThenExertion() {
		return thenExertion;
	}

	/**
	 * Returns the else exertion component, can be a Conditional, job or task.
	 * 
	 * @return Exertion
	 */
	public Exertion getElseExertion() {
		return elseExertion;
	}

	/**
	 * Adds an exception to the IfExertion which sets it to the thenExertion by
	 * default.
	 * 
	 * @return boolean value
	 * @param e
	 *            the exception to be added
	 * @see sorcer.service.ServiceExertion#reportException(java.lang.Exception)
	 */
	public void reportException(Exception e) {
		((ServiceExertion) thenExertion).reportException(e);
	}

	/**
	 * Returns the context name of the ifExertion, which uses the thenExertion
	 * by default.
	 * 
	 * @return String the name of the context
	 * @see sorcer.service.ServiceExertion#getContextName()
	 */
	public String getContextName() {
		return thenExertion.getContext().getName();
	}

	/**
	 * Returns the context of the IfExertion, where the thenExertion and
	 * elseExertion's are also inserted too.
	 * 
	 * @return ServiceContext
	 */
	public Context getContext() {
		return ifContext;
	}

	/**
	 * Sets the context of the IfExertion by default.
	 * 
	 * @param context
	 *            the context to replace the current context
	 */
	public void setConditionalContext(Context context) {
		this.ifContext = context;
	}

	/**
	 * Returns true if the exertion is a job, otherwise returns false.
	 * 
	 * @return boolean
	 * @see sorcer.service.ServiceExertion#isJob()
	 */
	public boolean isJob() {
		return ((ServiceExertion) thenExertion).isJob();
	}

	/**
	 * Returns true if the exertion is a task, otherwise returns false.
	 * 
	 * @return boolean
	 * @see sorcer.service.ServiceExertion#isTask()
	 */
	public boolean isTask() {
		return ((ServiceExertion) thenExertion).isTask();
	}

	/**
	 * Sets the Owner ID of the IfExertion which sets the thenExertion by
	 * default.
	 * 
	 * @param id
	 *            The unique id
	 * @see sorcer.service.ServiceExertion#setOwnerId(java.lang.String)
	 * @see UUID#randomUUID()
	 */
	public void setOwnerId(String id) {
		((ServiceExertion) thenExertion).setOwnerId(id);
	}

	/**
	 * Returns the ServiceMethod of the base exertion. Uses the thenExertion by
	 * default.
	 * 
	 * @return Signature
	 * @see NetSignature
	 * @see RemoteServiceSignature
	 */
	public Signature getProcessSignature() {
		return thenExertion.getProcessSignature();
	}

	/**
	 * This method allows to setup the condition without passing the condition
	 * object. It is done by first setting the variables in the condition
	 * boolean expression. The variables will be the reference to the actual
	 * data nodes in the context by specifying the context data nodes path as an
	 * argument.
	 * 
	 * @param variableName
	 *            Name of the variable for the conditional expression
	 * @param contextPath
	 *            Context path for a given variable name
	 * @see ServiceContext
	 * @see JEP
	 */
	public void setConditionVariable(String variableName, String contextPath) {
		mapReference.put(variableName, contextPath);
	}

	/**
	 * Sets the boolean expression to be evaluated by the IfExertion if the
	 * Condition is not set. The variables in the expression is set by using
	 * {@link IfExertion#setVariable(String, String)}.
	 * 
	 * @param expression
	 *            the boolean expression
	 * @see {@link WhileExertion#setVariable(String, String)}
	 * @see JEP
	 */
	public void setCondition(String expression) {
		this.expression = expression;
	}

	/**
	 * Evaluates the string expression from the given variables which correlate
	 * to a specific data node.
	 * 
	 * @return double result from the expression
	 */
	protected double evalCondition() {
		JEP jepParser = new JEP();
		Context context = this.getContext();
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
	 * Determines if the given condition is true or false.
	 * 
	 * @return boolean
	 * @throws ExertionException
	 *             exception
	 */
	public boolean isTrue() throws ExertionException {

		if (condition != null) {
			try {
				condition.setConditionalContext(this.getContext());
			} catch (ContextException ce) {
				testLog.finest("Unable to setContext on Condition: " + ce);
				ce.printStackTrace();
			}
			boolean conditionResult = condition.isTrue();
			testLog.finest("isTrue(): " + conditionResult);
			return conditionResult;
		}

		else if (expression != null) {
			double result = evalCondition();

			if (result == 0 || result == 1) {
				testLog.info("isTrue(): " + (result == 1));
				return result == 1;
			} else {
				throw new ExertionException(
						"Boolean expression is not a valid condition "
								+ WhileExertion.class.getClass().getName());
			}
		}

		else {
			throw new ExertionException("missing condition in"
					+ WhileExertion.class.getClass().getName());
		}
	}

	/**
	 * Returns the Map reference of variable names and their associated context
	 * path.
	 * 
	 * @return Map the reference between the given variable names and data node
	 *         path
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
	 * @return String the boolean expression
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
	 * Returns which exertion (then,else) has been executed by the IfExertion by
	 * checking the getStatus of these Exertion.
	 * 
	 * @return Exertion
	 */
	public Exertion getResult() {
		Exertion exertion = null;
		try {
			exertion = (Exertion) ifContext.getValue("exertion/done");
		} catch (ContextException e) {
			e.printStackTrace();
		}
		return exertion;
	}

	/**
	 * Sets the elseExertion into the IfExertion.
	 * 
	 * @param elseExertion
	 *            Exertion component
	 */
	public void setElseExertion(Exertion elseExertion) {
		this.elseExertion = elseExertion;
		try {
			ifContext.putValue("in/context/elseExertion", elseExertion
					.getContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the elseExertion into the IfExertion.
	 * 
	 * @param thenExertion
	 *            Exertion component
	 */
	public void setThenExertion(Exertion thenExertion) {
		this.thenExertion = thenExertion;
		try {
			ifContext.putValue("in/context/thenExertion", thenExertion
					.getContext());
		} catch (ContextException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return true if this <code>IfExertion</code> is a tree.
	 * 
	 * @param visited
	 *            a set of visited exertions
	 * @return true if this <code>IfExertion</code> is a tree
	 * @see Exertion#isTree()
	 */
	public boolean isTree(Set visited) {
		visited.add(this);
		if (visited.contains(thenExertion)
				|| !((ServiceExertion) thenExertion).isTree(visited)) {
			return false;
		}
		if (visited.contains(elseExertion)
				|| !((ServiceExertion) elseExertion).isTree(visited)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an <code>IfExertion</code> in the specified format. Some
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

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#linkContext(sorcer.service.Context, java.lang.String)
	 */
	@Override
	public Context linkContext(Context context, String path)
			throws ContextException {
		throw new ContextException("linkContext operation not supported");
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Exertion#getExceptions()
	 */
	@Override
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
		list.add(thenExertion);
		list.add(elseExertion);
		return list;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.ServiceExertion#getExertions(java.util.List)
	 */
	@Override
	public List<Exertion> getExertions(List<Exertion> exs) {
			exs.add(thenExertion);
			exs.add(elseExertion);
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
