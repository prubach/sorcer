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
package sorcer.arithmetic.requestor;

import org.codehaus.groovy.control.CompilationFailedException;
import sorcer.core.requestor.ServiceRequestor;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;

import java.io.File;
import java.io.IOException;

public class ArithmeticNetletReq extends ServiceRequestor {

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