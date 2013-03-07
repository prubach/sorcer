package sorcer.falcon.validation.differentiation.requestor;

import sorcer.falcon.validation.base.Function;

/**
 * Implementation of the Function interface. This class uses the command pattern
 * for evaluating the function. In this case the function is a method.
 * 
 * @author Michael Alger
 */
public class FunctionImpl implements Function {

	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor
	 */
	public FunctionImpl() {
		//do nothing
	}

	/**
	 * Returns the scalar value of the function, which uses the command pattern
	 * @param x double
	 * @return double
	 */
	public double evaluate(double x) {
		return x*x*x;
	}
}
