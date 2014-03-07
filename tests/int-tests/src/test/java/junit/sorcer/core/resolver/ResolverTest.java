package junit.sorcer.core.resolver;/*
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


import org.junit.Assert;
import org.junit.Test;
import org.rioproject.resolver.Resolver;
import org.rioproject.resolver.ResolverException;
import org.rioproject.resolver.ResolverHelper;
import sorcer.core.SorcerConstants;

/**
 * @author Rafał Krupiński
 */
public class ResolverTest {
    @Test
    public void testResolver() throws ResolverException {
        Resolver resolver = ResolverHelper.getResolver();
        String[] cp = resolver.getClassPathFor("org.sorcersoft.sorcer:ju-arithmetic-cfg-all:" + SorcerConstants.SORCER_VERSION);
        Assert.assertTrue(cp != null);
        for (String cpe : cp) {
            System.out.println(cpe);
        }
        Assert.assertTrue(cp.length > 2);
    }
}
