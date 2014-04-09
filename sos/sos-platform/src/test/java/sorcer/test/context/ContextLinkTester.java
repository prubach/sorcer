/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
 * Copyright 2014 SorcerSoft.com S. A.
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

package sorcer.test.context;

import org.junit.Ignore;
import org.junit.Test;
import sorcer.core.context.ContextLink;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;

import static org.junit.Assert.assertEquals;

/**
 * Example how use the ContextLink class
 *
 * @author Michael Alger
 * @author Rafał Krupiński - update to junit
 */
public class ContextLinkTester {

	/**
	 * Example how to staticly attached a external data node (from another
	 * context) to the main context by explicitly creating ContextLink
	 */
    @Test
    @Ignore("didn't work before migrating to junit")
    public void test1() throws ContextException {
		Context mainContext = new ServiceContext("main");
		Context leafContext = new ServiceContext("leaf");
        String originalPath = "leaf/message/test";
        String linkedPath = "in/context/1";

        // insert a test node
        leafContext.putValue(originalPath,
                "Hello from the leafContext! (test1)");

        // insert the context, and the offset.
        // offset is the path of the data node in the leaf context which
        // you wish to attach to the main context
        ContextLink contextLink = new ContextLink(leafContext, originalPath);

        // insert the contextLink to the main context on a designated path
        mainContext.putValue(linkedPath, contextLink);

        // retreive the data node in the leaf context automatically
        // using the offset (path) assigned
        assertEquals(leafContext.getValue(originalPath), mainContext.getValue(linkedPath));
	}// test1

	/**
	 * Example how to staticly attached a external data node (from another
	 * context) to the main context by using the putLink() method
	 */
    @Test
    @Ignore("didn't work before migrating to junit")
    public void test2() throws ContextException {
		Context mainContext = new ServiceContext("main");
		Context leafContext = new ServiceContext("leaf");

        // insert a test node
        String originPath = "leaf/message/test";
        leafContext.putValue(originPath,
                "Hello from the leafContext! (test2)");

        String linkPath = "in/context/2";
        mainContext.putLink(linkPath, leafContext,
                originPath);

        assertEquals(leafContext.getValue(originPath), mainContext.getValue(linkPath));
    }

	/**
	 * Example how to use the ContextList in a dynamic fashion
	 */
    @Test
    public void test3() throws ContextException {
		Context mainContext = new ServiceContext("main");
		Context leafContext = new ServiceContext("leaf");
        ContextLink contextLink;

        // insert a test node
        leafContext.putValue("attachedNode/message/test",
                "Hello! from the leafContext! (test3)");
        String inputValue1 = "Hi! from the leafContext! (test3)";
        leafContext.putValue("attachedNode/message/test1",
                inputValue1);

        // insert the context, and the offset.
        // offset is the path of the data node in the leaf context which
        // you wish to attach to the main context
        contextLink = new ContextLink(leafContext, "attachedNode");

        // insert the contextLink to the main context on a designated path
        mainContext.putValue("in/context/1", contextLink);

        // retreive the data node in the leaf context automatically
        // using the offset (path) assigned
        System.out
                .println("Link Context node: "
                        + mainContext
                        .getValue("in/context/1/attachedNode/message/test1"));

        assertEquals(inputValue1, mainContext.getValue("in/context/1/attachedNode/message/test1"));

        String inputValue2 = "new node(test3)";
        mainContext.putValue("in/context/1/attachedNode/message/test2", inputValue2);

        assertEquals(inputValue2, leafContext.getValue("attachedNode/message/test2"));

	}

	/**
	 * Another example how to use the ContextList in a dynamic fashion using
	 * putLink() method
	 */
    @Test
    public void test4() throws ContextException {
        Context mainContext = new ServiceContext("main");
        Context leafContext = new ServiceContext("leaf");

        // insert a test node
        String inputValue = "Hello from the leafContext! (test4)";
        leafContext.putValue("leafContext/message/test",
                inputValue);

        mainContext.putLink("in/context/2", leafContext, "leafContext");

        assertEquals(inputValue, mainContext.getValue("in/context/2/leafContext/message/test"));
	}
}
