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

package sorcer.file;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

public class ScratchDirManagerTest {

    @Test
    public void testGetNewScratchDir() throws Exception {
        long start = System.currentTimeMillis();
        File root = new File(FileUtils.getTempDirectory(), "scratch");
        ScratchDirManager manager = new ScratchDirManager(root, 0);
        File testDir = manager.getNewScratchDir("a");

        FileUtils.forceMkdir(testDir);
        Thread.sleep(1000);
        manager.cleanup0(start);
    }
}
