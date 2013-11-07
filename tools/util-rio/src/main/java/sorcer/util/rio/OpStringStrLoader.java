package sorcer.util.rio;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import org.apache.commons.io.FileUtils;
import org.rioproject.impl.opstring.OpStringLoader;
import org.rioproject.opstring.OperationalString;

import java.io.File;

/**
 * Extend Rio OpStringLoader with parsing groovy operational string from String.
 * <p/>
 *
 * @author Rafał Krupiński
 */
public class OpStringStrLoader extends OpStringLoader {
    /**
     * Create temporary file, call the parser with that file as an argument, and, unless there was an exception, delete the file.
     *
     * @param groovyOpString operational string as String
     * @return parsed contents of the operational string
     * @throws Exception           any exception from Rio parser
     * @throws java.io.IOException on error on creating or writing the temp file.
     */
    public OperationalString[] parseOperationalString(String groovyOpString) throws Exception {
        File opStringFile = File.createTempFile("almanac_opstring_", ".groovy");
        FileUtils.write(opStringFile, groovyOpString);
        return parseOperationalString(opStringFile);
    }
}
