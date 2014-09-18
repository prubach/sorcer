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
package sorcer.ex5.requestor;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.ex5.provider.Adder;
import sorcer.junit.*;
import sorcer.service.Signature;
import sorcer.service.SignatureException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;
import static sorcer.eo.operator.*;

/**
 * @author Mike Sobolewski
 */

@SuppressWarnings("rawtypes")
@RunWith(SorcerSuite.class)
@Category(SorcerClient.class)
@ExportCodebase({
        "org.sorcersoft.sorcer:sorcer-api",
        "org.sorcersoft.sorcer:ex5-api"
})
@SorcerServiceConfigurations({
        @SorcerServiceConfiguration({
                ":ex5-cfg-adder",
        }),
        @SorcerServiceConfiguration({
                ":ex5-cfg-all",
        }),
        @SorcerServiceConfiguration({
                ":ex5-cfg-one-bean",
        })
})
public class SignatureTest {
	private final static Logger logger = LoggerFactory
			.getLogger(SignatureTest.class.getName());

	@Test
	public void netProviderTest() throws SignatureException  {
		Signature s3 = sig("add", Adder.class);
		logger.info("provider of s3: " + provider(s3));
		assertTrue(provider(s3) instanceof Adder);
	}
}
