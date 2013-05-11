package sorcer.ex5.requestor;

import static org.junit.Assert.assertEquals;

import java.rmi.RMISecurityManager;
import java.util.logging.Logger;

import org.junit.Test;

import sorcer.core.SorcerConstants;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.signature.NetSignature;
import sorcer.service.Context;
import sorcer.service.Exerter;
import sorcer.service.Job;
import sorcer.service.Task;
import sorcer.util.Sorcer;

/**
 * @author Mike Sobolewski
 */
@SuppressWarnings({ "rawtypes" })
public class ArithmeticExertServiceTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(ArithmeticExertServiceTest.class.getName());

	static {
        System.setProperty("java.security.policy", System.getenv("SORCER_HOME")
                + "/configs/sorcer.policy");
        System.setSecurityManager(new RMISecurityManager());
        Sorcer.setCodeBaseByArtifacts(new String[] {
                "org.sorcersoft.sorcer:sos-platform",
                "org.sorcersoft.sorcer:ex5-api" });
        System.out.println("CLASSPATH :" + System.getProperty("java.class.path"));
	}
	
	@Test
	public void exertExerter() throws Exception {
		Job exertion = NetArithmeticReqTest.getJobInJobNetArithmeticJob();
		Task task = new NetTask("exert", new NetSignature("exert",
				Exerter.class),
				new ServiceContext(exertion));
		Task result = (Task) task.exert();
		// logger.info("result: " + result);
		// logger.info("return value: " + result.getReturnValue());
	
		Context out = (Context) result.getContext();
//		logger.info("out context: " + out);
		logger.info("1job1task/subtract/result/value: "
				+ out.getValue(
						"1job1task/subtract/result/value"));
		assertEquals(
				out.getValue("1job1task/subtract/result/value"),
				400.0);
	}
}
