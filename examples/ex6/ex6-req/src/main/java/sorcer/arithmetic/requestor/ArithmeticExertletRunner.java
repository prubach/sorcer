package sorcer.arithmetic.requestor;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

import sorcer.core.requestor.ExertletRunner;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;

public class ArithmeticExertletRunner extends ExertletRunner {

	/* (non-Javadoc)
	 * @see sorcer.core.requestor.ExertionRunner#getExertion(java.lang.String[])
	 */
	@Override
	public Exertion getExertion(String... args) throws ExertionException {
		try {
			exertion = (Exertion)evaluate(new File(getProperty("exertion.filename")));
		} catch (CompilationFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exertion;
	}

	public void postprocess(String... args) {
		super.postprocess();
		logger.info("<<<<<<<<<< f5 dataContext: \n" + ((Job)exertion).getExertion("f5").getDataContext());
	}
}