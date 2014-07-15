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


import sorcer.core.deploy.ServiceDeployment;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ObjectSignature;
import sorcer.util.Sorcer;

import static sorcer.service.Signature.Type;

/**
 * Methods for creating Signature objects; extracted from sorcer.eo.operator.
 * Use it instead of operator in sorcer code.
 *
 * @author Rafał Krupiński
 */
public class SignatureFactory {

    public static Signature sig(String operation, Class<?> serviceType,
                                String version, String providerName, Object... parameters)
            throws SignatureException {
        /*#TARGET sig_operation_servicetype_providername_parameters*/
        Signature sig = null;
        if (serviceType.isInterface()) {
            sig = new NetSignature(operation, serviceType, version,
                    (providerName!=null ? Sorcer.getActualName(providerName) : null));
        } else {
            sig = new ObjectSignature(operation, serviceType);
        }
            if (parameters.length > 0) {
            for (Object o : parameters) {
                if (o instanceof Type) {
                    sig.setType((Type) o);
                } else if (o instanceof ReturnPath) {
                    sig.setReturnPath((ReturnPath) o);
                } else if (o instanceof ServiceDeployment) {
                    sig.setDeployment((ServiceDeployment)o);
                }

            }
        }
        return sig;
        /*#*/
    }


    public static Signature sig(String operation, Class serviceType, String version) throws SignatureException {
        return sig(operation, serviceType, version, null, Type.SRV);
    }

    public static Signature sig(Class<?> serviceType, ReturnPath returnPath)
            throws SignatureException {
        /*#TARGET sig_class_returnpath*/
        Signature sig = null;
        if (serviceType.isInterface()) {
            sig = new NetSignature("service", serviceType);
        } else if (Executor.class.isAssignableFrom(serviceType)) {
            sig = new ObjectSignature("execute", serviceType);
        } else {
            sig = new ObjectSignature(serviceType);
        }
        if (returnPath != null)
            sig.setReturnPath(returnPath);
        return sig;
        /*#*/
    }

}
