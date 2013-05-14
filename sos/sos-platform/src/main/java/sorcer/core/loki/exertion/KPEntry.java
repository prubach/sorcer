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
package sorcer.core.loki.exertion;

import java.security.PublicKey;

import net.jini.core.entry.Entry;

public class KPEntry implements Entry {

    private static final long serialVersionUID = -3134975993027375539L;

    public Boolean isCreator;

    public byte[] keyPair;

    public PublicKey publicKey;

    public String GroupSeqId;

    static public KPEntry get(Boolean iscreator, byte[] keypair,
                              PublicKey pk, String GSUID) {
        KPEntry KP = new KPEntry();
        KP.isCreator = iscreator;
        KP.keyPair = keypair;
        KP.publicKey = pk;
        KP.GroupSeqId = GSUID;
        return KP;
    }

    public String getName() {
        return "KeyPair and KeyAgreement Exertion";
    }

}
