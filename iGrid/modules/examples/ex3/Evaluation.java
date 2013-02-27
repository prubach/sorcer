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

package sorcer.core.context.model;

import java.io.Serializable;

import sorcer.vfe.util.Wrt;


public class Evaluation implements Serializable {
	
	protected String varName;

	protected String evaluationName;
	
	protected String evalutorName;
	
	protected String filterName;
	
	// selected gradient for this evaluation
	protected String gradientName;
	
	protected Wrt wrt;

	protected Differentiation differentiation;

	public Evaluator() {
		// evaluationName undefined
	}
	
	public Evaluator(String evaluationName) {
		this.evaluationName = evaluationName;
	}
	
	public Evaluator(String varName, String evaluationName, String evaluatorName, String filterName, String gradientName) {
		this.varName = varName;
		this.evaluationName = evaluationName;
		this.evalutorName = evaluatorName;
		this.filterName = filterName;
		this.gradientName = gradientName;
	}
	
	public Evaluator(String varName, String evaluationName) {
		this.varName = varName;
		this.evaluationName = evaluationName;
	}
	
	public Evaluator(String varName, String evaluationName, String gradientName) {
		this(varName, evaluationName);
		this.gradientName = gradientName;
	}
	
	public Evaluator(String varName, Wrt wrt, String evaluationName, String gradientName) {
		this(varName, evaluationName, gradientName);
		this.wrt = wrt;
	}
	
	public Evaluator(Evaluator eval) {
		varName = eval.varName;
		evaluationName = eval.evaluationName;
		evalutorName = eval.evalutorName;
		filterName = eval.filterName;
		gradientName = eval.gradientName;
		if (eval.differentiation != null)
			differentiation = new Differentiation(eval.differentiation);
	}
		
	public String getEvaluationName() {
		return evaluationName;
	}

	public void setEvaluationName(String evaluationName) {
		this.evaluationName = evaluationName;
	}
	
	public String getEvaluatorName() {
		if (evalutorName == null)
			return evaluationName;
		else
			return evalutorName;
	}
	
	public String getFilterName() {
		return filterName;
	}

	public void setEvaluatorName(String name) {
		 evalutorName = name;
		 if (differentiation != null)
			 differentiation.evaluatorName = name;
	}
	
	public void setFilterName(String name) {
		filterName = name;
	}

	/**
	 * <p>
	 * Returns the with-respect-to var name.
	 * </p>
	 * 
	 * @return the wrt name
	 */
	public Wrt getWrt() {
		return wrt;
	}

	/**
	 * <p>
	 * Assigns the with-respect-to var name.
	 * </p>
	 * 
	 * @param wrt
	 *            the wrt name to set
	 */
	public void setWrt(Wrt wrt) {
		this.wrt = wrt;
	}

	public void setWrt(String wrt) {
		this.wrt = new Wrt(wrt);
	}
	
	/**
	 * <p>
	 * Returns the gradient name (a column name) in the corresponding derivative evaluator.
	 * Each column specifies a list of derivative evaluators associated with this name.
	 * </p>
	 * 
	 * @return the gradientName
	 */
	public String getGradientName() {
		return gradientName;
	}

	/**
	 * <p>
	 * Sets the name (a column name in) the corresponding derivative evaluator.
	 * Each column specifies a list of derivative evaluators associated with this name.
	 * </p>
	 * 
	 * @param gradientName
	 *            the gradientName to set
	 */
	public void setGradientName(String gradientName) {
		this.gradientName = gradientName;
	}

	/**
	 * <p>
	 * Returns a variable name for this evaluation.
	 * </p>
	 * 
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}

	/**
	 * <p>
	 * Assigns a variable name for this evaluation.
	 * </p>
	 * 
	 * @param varName
	 *            the varName to set
	 */
	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	/**
	 * <p>
	 * Returns a derivative evaluator declaration for this variable realization.
	 * </p>
	 * 
	 *  none
	 * @return the derivative
	 */
	public Differentiation getDifferentiation() {
		return differentiation;
	}

	/**
	 * <p>
	 * Assigns a derivative evaluator declaration for this variable realization
	 * </p>
	 * 
	 * @param differentiation
	 *            the differentiation to set
	 */
	public void setDifferentiation(Differentiation differentiation) {
		this.differentiation = differentiation;
	}
	

	@Override
	public String toString() {
		return "Evaluation: " + evaluationName + (varName == null ? "" : ":" + varName)  + ":" 
			+ evalutorName + ":" + filterName + ", " + differentiation;
	}
	
}
