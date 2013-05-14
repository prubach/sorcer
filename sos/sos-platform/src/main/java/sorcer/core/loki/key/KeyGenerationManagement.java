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
package sorcer.core.loki.key;

import net.jini.id.Uuid;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.Map;
import java.util.Vector;

/**
 * The Key Generation Management provides the interface for
 * both key and key agremment generation, as well as the
 * framework specific implementation of the complimentary
 * compound key and the shared key object.
 * 
 * @author Daniel Kerr
 */

public interface KeyGenerationManagement
{
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * Generate key pair
	 * 
	 * @return the generated key pair
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @see InvalidAlgorithmParameterException, NoSuchAlgorithmException
	 */
	public KeyPair genKeyPair() throws InvalidAlgorithmParameterException,NoSuchAlgorithmException;
	/**
	 * Generate key agreement
	 * 
	 * @param myKP the previously generated key pair
	 * @return generated key agreement
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @see InvalidKeyException, NoSuchAlgorithmException
	 */
	public KeyAgreement genKeyAgreement(KeyPair myKP) throws InvalidKeyException,NoSuchAlgorithmException;
	
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * Generate complimentary compound keys
	 * 
	 * @param ids
	 * @param pairs
	 * @return
	 * @throws InvalidKeyException;
	 * @see InvalidKeyException;
	 */
	public Map<Uuid,Key> genCompoundKeys(Uuid[] ids,KeyPair[] pairs) throws InvalidKeyException,NoSuchAlgorithmException;
	
	public Map<Uuid,Key> genCompoundKeys(Vector<Uuid> ids,Vector<KeyPair> pairs) throws InvalidKeyException,NoSuchAlgorithmException;
	/**
	 * Generate shared key based on the complimentary compound key
	 * 
	 * @param agree
	 * @param compKey
	 * @return
	 * @throws InvalidKeyException
	 * @see InvalidKeyException
	 */
	public KeyAgreement genSharedKey(KeyAgreement agree,Key compKey) throws InvalidKeyException;
	
	//------------------------------------------------------------------------------------------------------------
}
