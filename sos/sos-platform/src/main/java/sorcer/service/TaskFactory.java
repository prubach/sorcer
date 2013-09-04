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


import sorcer.core.context.ControlContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.exertion.NetTask;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ObjectSignature;
import sorcer.core.signature.ServiceSignature;

import java.util.ArrayList;
import java.util.List;

import static sorcer.service.Strategy.Access;
import static sorcer.service.Strategy.Flow;
import static sorcer.util.UnknownName.getUnknown;

/**
 * Methods extracted from socer.eo.operator
 *
 * @author Rafał Krupiński
 */
public class TaskFactory {
    public static Task task(String name, Signature signature, Context context)
            throws SignatureException {
        if (signature instanceof NetSignature) {
            return new NetTask(name, signature, context);
        } else if (signature instanceof ObjectSignature) {
            return new ObjectTask(name, signature, context);
        } else
            return new Task(name, signature, context);
    }

    public static <T> Task task(String name, T... elems)
            throws ExertionException {
        Context context = null;
        List<Signature> ops = new ArrayList<Signature>();
        String tname;
        if (name == null || name.length() == 0)
            tname = getUnknown();
        else
            tname = name;
        Task task = null;
		Access access = null;
		Flow flow = null;
        ControlContext cc = null;
        for (Object o : elems) {
            if (o instanceof ControlContext) {
                cc = (ControlContext) o;
            } else if (o instanceof Context) {
                context = (Context) o;
            } else if (o instanceof Signature) {
                ops.add((Signature) o);
            } else if (o instanceof String) {
                tname = (String) o;
			} else if (o instanceof Access) {
				access = (Access) o;
			} else if (o instanceof Flow) {
				flow = (Flow) o;
            }
        }
        Signature ss = null;
        if (ops.size() == 1) {
            ss = ops.get(0);
        } else if (ops.size() > 1) {
            for (Signature s : ops) {
                if (s.getType() == Signature.SRV) {
                    ss = s;
                    break;
                }
            }
        }
        if (ss != null) {
            if (ss instanceof NetSignature) {
                try {
					task = new NetTask(tname, (NetSignature) ss);
                } catch (SignatureException e) {
                    throw new ExertionException(e);
                }
            } else if (ss instanceof ObjectSignature) {
                try {
                    task = task((ObjectSignature) ss);
                } catch (SignatureException e) {
                    throw new ExertionException(e);
                }
                task.setName(tname);
            } else if (ss instanceof ServiceSignature) {
                task = new Task(tname, ss);
            }
            ops.remove(ss);
        }
        for (Signature signature : ops) {
            task.addSignature(signature);
        }

        if (context == null) {
            context = new ServiceContext();
        }
        task.setContext(context);

        if (access != null) {
            task.setAccess(access);
        }
        if (flow != null) {
            task.setFlow(flow);
        }
        if (cc != null) {
            task.updateStrategy(cc);
        }
        return task;
    }

    public static ObjectTask task(ObjectSignature signature)
            throws SignatureException {
        return new ObjectTask(signature.getSelector(),
                signature);
    }

}
