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

package sorcer.river.protocol;

import net.jini.entry.AbstractEntry;

import java.net.URLStreamHandler;
import java.rmi.MarshalledObject;

/**
 * @author Rafał Krupiński
 */
public class ProtocolHandlerEntry extends AbstractEntry {
    private static final long serialVersionUID = 1228620582569814195L;
    public String[] protocols;
    public MarshalledObject<URLStreamHandler> handler;

    public ProtocolHandlerEntry() {
    }

    public ProtocolHandlerEntry(String[] protocols, MarshalledObject<URLStreamHandler> handler) {
        this.protocols = protocols;
        this.handler = handler;
    }

    public ProtocolHandlerEntry(String protocol, MarshalledObject<URLStreamHandler> handler) {
        this(new String[]{protocol}, handler);
    }
}
