package sorcer.service;
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


import static sorcer.service.Signature.Type;

/**
 * Methods for creating Signature objects; extracted from sorcer.eo.operator.
 * Use it instead of operator in sorcer code.
 *
 * @author Rafał Krupiński
 */
public class SignatureFactory {

    public static Signature sig(String operation, Class<?> serviceType,
                                String providerName, Object... parameters)
            throws SignatureException {
        /*#TARGET sig_operation_servicetype_providername_parameters*/
        return null;
        /*#*/
    }


    public static Signature sig(String operation, Class serviceType) throws SignatureException {
        return sig(operation, serviceType, null, Type.SRV);
    }

    public static Signature sig(Class<?> serviceType, ReturnPath returnPath)
            throws SignatureException {
        /*#TARGET sig_class_returnpath*/
        return null;
        /*#*/
    }

}
