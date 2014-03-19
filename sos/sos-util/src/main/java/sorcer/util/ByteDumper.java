package sorcer.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyright 2014 Sorcersoft.com S.A.
 * Copyright 2003-2013 the original author or authors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ByteDumper implements Runnable {
    private InputStream in;
    private OutputStream out;

    public ByteDumper(InputStream in, OutputStream out) {
        assert in != null;
        assert out != null;
        this.in = new BufferedInputStream(in);
        this.out = out;
    }

    public void run() {
        byte[] buf = new byte[8192];
        int next;
        try {
            while ((next = in.read(buf)) != -1) {
                out.write(buf, 0, next);
                out.flush();
            }
        } catch (IOException e) {
            if (!"Stream closed".equals(e.getMessage()))
                throw new RuntimeException("Exception while dumping process stream", e);
        }
    }
}

