package junit.sorcer.core.signature;

import static org.junit.Assert.assertTrue;
import static sorcer.eo.operator.provider;
import static sorcer.eo.operator.sig;
//import static sorcer.vo.operator.expression;
//import static sorcer.vo.operator.groovy;
//import static sorcer.vo.operator.var;
//import static sorcer.vo.operator.vars;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import junit.sorcer.core.provider.AdderImpl;

import org.junit.Ignore;
import org.junit.Test;

import sorcer.arithmetic.provider.Adder;
import sorcer.service.ContextException;
import sorcer.service.ExertionException;
import sorcer.service.Signature;
import sorcer.service.SignatureException;
import sorcer.util.Sorcer;
//import sorcer.vfe.Var;
//import sorcer.vfe.evaluator.ExpressionEvaluator;
//import sorcer.vfe.filter.ListFilter;

/**
 * @author Mike Sobolewski
 */

public class SignatureTest {
	private final static Logger logger = Logger
			.getLogger(SignatureTest.class.getName());

	static {
		System.setProperty("java.util.logging.config.file",
				System.getenv("IGRID_HOME") + "/configs/sorcer.logging");
		System.setProperty("java.security.policy", System.getenv("IGRID_HOME")
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBase(new String[] { "arithmetic-beans.jar" });
	}
	
	@Test
	public void providerTest() throws ExertionException, ContextException, SignatureException {
		
		Signature s1 = sig("add", new AdderImpl());
		//logger.info("provider of s1: " + provider(s1));
		assertTrue(provider(s1) instanceof  AdderImpl);
		
		Signature s2 = sig("add", AdderImpl.class);
		//logger.info("provider of s2: " + provider(s2));
		assertTrue(provider(s2) instanceof  AdderImpl);

//		Signature s4 = sig(groovy("new Date()"));
//		//logger.info("provider of s4: " + provider(s4));
//		assertTrue(provider(s4) instanceof  ExpressionEvaluator);
//		
//		Signature s5 = sig(new ListFilter(4));
//		//logger.info("provider of s5: " + provider(s5));
//		assertTrue(provider(s5) instanceof  ListFilter);
//		
//		Signature s6 = sig(var("x3", expression("x3-e", "x1 - x2", vars("x1", "x2"))));
//		logger.info("provider of s6: " + provider(s6));
//		assertTrue(provider(s6) instanceof Var);

	}
	
	@Ignore
	@Test
	public void netProviderTest() throws SignatureException  {
		Signature s3 = sig("add", Adder.class);
		logger.info("provider of s3: " + provider(s3));
		assertTrue(provider(s3) instanceof Adder);
	}
	
}