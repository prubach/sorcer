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

package sorcer.util.io;

import sorcer.org.apache.commons.io.output.ProxyWriter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Rafał Krupiński
 */
abstract public class LineWriter extends ProxyWriter {
    private final StringBuffer buf;

    public LineWriter() {
        super(new StringWriter());
        buf = ((StringWriter) super.out).getBuffer();
    }

    @Override
    protected void afterWrite(int n) throws IOException {
        synchronized (buf) {
            int eol;
            while ((eol = buf.indexOf("\n")) != -1) {
                int eom = eol - 1;
                if (eol > 0 && buf.charAt(eol - 1) == '\r')
                    --eom;
                writeLine(buf.substring(0, eom + 1));
                flush();
                buf.delete(0, eol + 1);
            }
        }
    }

    public abstract void writeLine(String line) throws IOException;
}
