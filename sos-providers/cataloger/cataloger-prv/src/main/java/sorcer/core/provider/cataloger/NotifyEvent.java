/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.core.provider.cataloger;

import java.util.*;
import net.jini.core.event.*;
import java.rmi.*;

import sorcer.core.Cataloger;

public class NotifyEvent extends RemoteEvent {
    private Hashtable services;
    
    public NotifyEvent(Cataloger source, Hashtable services,int eventID, long seqNum,MarshalledObject handback) {
	super(source,eventID,seqNum,handback);
	this.services = services;
    }

    public Hashtable getServices() {
	return services;
    }
}
