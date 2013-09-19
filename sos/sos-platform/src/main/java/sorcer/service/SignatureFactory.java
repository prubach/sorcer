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


import net.jini.core.entry.Entry;
import sorcer.core.SorcerEnv;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ObjectSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.util.MavenUtil;
import sorcer.util.Sorcer;

import java.util.List;

import static sorcer.service.Signature.Type;

/**
 * Methods for creating Signature objects; extracted from sorcer.eo.operator.
 * Use it instead of operator in sorcer code.
 *
 * @author Rafał Krupiński
 */
public class SignatureFactory {
    public static Signature sig(Class<?> serviceType, String providerName,
                                Object... parameters) throws SignatureException {
        return sig(null, serviceType, Sorcer.getActualName(providerName), parameters);
    }

    public static Signature sig(String operation, Class<?> serviceType,
                                String providerName, Object... parameters)
            throws SignatureException {
        return sig(operation, serviceType, MavenUtil.findVersion(serviceType), providerName, parameters);
    }

    public static Signature sig(String operation, Class<?> serviceType, String version,
                                String providerName, Object... parameters)
            throws SignatureException {
        Signature sig;
        if (serviceType.isInterface()) {
            sig = new NetSignature(operation, serviceType, version, SorcerEnv.getActualName(providerName));
        } else {
            sig = new ObjectSignature(operation, serviceType);
        }
        // default Operation type = SERVICE
        sig.setType(Type.SRV);
        if (parameters.length > 0) {
            for (Object o : parameters) {
                if (o instanceof Type) {
                    sig.setType((Type) o);
                } else if (o instanceof ReturnPath) {
                    sig.setReturnPath((ReturnPath) o);
                }
            }
        }
        return sig;
    }

    public static Signature sig(String selector) throws SignatureException {
        return new ServiceSignature(selector);
    }

    public static Signature sig(String name, String selector)
            throws SignatureException {
        return new ServiceSignature(name, selector);
    }

    public static Signature sig(String operation, Class<?> serviceType,
                                Type type) throws SignatureException {
        return sig(operation, serviceType, (String) null, type);
    }

    public static Signature sig(String operation, Class<?> serviceType,
                                Strategy.Provision type) throws SignatureException {
        return sig(operation, serviceType, (String) null, type);
    }

    public static Signature sig(String operation, Class<?> serviceType,
                                List<Entry> attributes)
            throws SignatureException {
        NetSignature op = new NetSignature();
        op.setAttributes(attributes);
        op.setServiceType(serviceType);
        op.setSelector(operation);
        return op;
    }

    public static Signature sig(Class<?> serviceType) throws SignatureException {
        return sig(serviceType, (ReturnPath) null);
    }

    public static Signature sig(Class<?> serviceType, ReturnPath returnPath)
            throws SignatureException {
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
    }

    public static Signature sig(String operation, Class<?> serviceType,
                                ReturnPath resultPath) throws SignatureException {
        Signature sig = sig(operation, serviceType, Type.SRV);
        sig.setReturnPath(resultPath);
        return sig;
    }

    public static Signature sig(Exertion exertion, String componentExertionName) {
        Exertion component = exertion.getExertion(componentExertionName);
        return component.getProcessSignature();
    }


}
