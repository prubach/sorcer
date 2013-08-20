package sorcer.rio.cli;
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


import org.rioproject.tools.cli.OptionHandler;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rafał Krupiński
 */
public class PropertiesOptionHandler implements OptionHandler {
    @Override
    public String process(String s, BufferedReader bufferedReader, PrintStream printStream) {
        Properties properties = System.getProperties();
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            result.append(e.getKey()).append("\t= ").append(e.getValue()).append("\n");
        }
        return result.toString();
    }

    @Override
    public String getUsage() {
        return "props";
    }
}
