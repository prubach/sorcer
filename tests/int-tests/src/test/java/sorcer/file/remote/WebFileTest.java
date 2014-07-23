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

package sorcer.file.remote;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.core.context.ServiceContext;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.service.Context;

import java.io.File;

import static org.junit.Assert.*;

@Category(SorcerClient.class)
@RunWith(SorcerRunner.class)
public class WebFileTest {

    @Test
    public void testDoGetFile() throws Exception {
        Context c = new ServiceContext();
        c.putValue("data", RemoteFileFactory.INST.forFile(new File("/tmp/helo.txt")));

        Object data = c.getValue("data");
        assertNotEquals(sorcer.service.Context.none, data);
        System.out.println("data:" + ((RemoteFile)data).getValue());
    }
}
