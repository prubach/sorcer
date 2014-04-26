/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.core.service;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Rafał Krupiński
 */
public class ActualServiceBuilderTest {
    @Test
    @Ignore
    public void testContributeInterface() throws Exception {
        ActualServiceBuilder builder = new ActualServiceBuilder("sorcer.core.service.ServiceBean");
        builder.contributeInterface(new PingServiceContribution(), IServiceContribution.class);

        Object o = builder.get();
        assertTrue(o instanceof IServiceBean);
        assertTrue(o instanceof IServiceContribution);

        IServiceContribution serviceBean = (IServiceContribution) o;
        assertTrue(serviceBean.ping());
    }
}
