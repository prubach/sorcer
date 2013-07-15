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


import sorcer.co.tuple.Entry;
import sorcer.co.tuple.Tuple2;
import sorcer.core.context.ArrayContext;
import sorcer.core.context.ListContext;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.SharedAssociativeContext;
import sorcer.core.context.SharedIndexedContext;
import sorcer.co.tuple.Complement;
import sorcer.co.tuple.Args;
import sorcer.co.tuple.ParameterTypes;
import sorcer.co.tuple.target;
import sorcer.co.tuple.InEntry;
import sorcer.co.tuple.OutEntry;
import sorcer.co.tuple.InoutEntry;
import sorcer.co.tuple.DataEntry;

import java.util.ArrayList;
import java.util.List;

import static sorcer.util.UnknownName.getUnknown;

/**
 * @author Rafał Krupiński
 */
public class ContextFactory {
    public static <T> Context context(T... entries)
            throws ContextException {
        if (entries[0] instanceof Exertion) {
            Exertion xrt = (Exertion) entries[0];
            if (entries.length >= 2 && entries[1] instanceof String)
                xrt = ((Job) xrt).getComponentExertion((String) entries[1]);
            return xrt.getDataContext();
        } else if (entries[0] instanceof String
                && entries[1] instanceof Exertion) {
            return ((Job) entries[1]).getComponentExertion((String) entries[0])
                    .getDataContext();
        }
        String name = getUnknown();
        List<Tuple2<String, ?>> entryList = new ArrayList<Tuple2<String, ?>>();
        List<Context.Type> types = new ArrayList<Context.Type>();
        Complement subject = null;
        ReturnPath returnPath = null;
        Args cxtArgs = null;
        ParameterTypes parameterTypes = null;
        target target = null;
        for (T o : entries) {
            if (o instanceof Complement) {
                subject = (Complement) o;
            } else if (o instanceof Args
                    && ((Args) o).args.getClass().isArray()) {
                cxtArgs = (Args) o;
            } else if (o instanceof ParameterTypes
                    && ((ParameterTypes) o).parameterTypes.getClass().isArray()) {
                parameterTypes = (ParameterTypes) o;
            } else if (o instanceof target) {
                target = (target) o;
            } else if (o instanceof Tuple2) {
                entryList.add((Tuple2) o);
            } else if (o instanceof ReturnPath) {
                returnPath = (ReturnPath) o;
            } else if (o instanceof Context.Type) {
                types.add((Context.Type) o);
            } else if (o instanceof String) {
                name = (String) o;
            }
        }
        Context cxt = null;
        if (types.contains(Context.Type.ARRAY)) {
            if (subject != null)
                cxt = new ArrayContext(name, subject.path(), subject.value());
            else
                cxt = new ArrayContext(name);
        } else if (types.contains(Context.Type.LIST)) {
            if (subject != null)
                cxt = new ListContext(name, subject.path(), subject.value());
            else
                cxt = new ListContext(name);
        } else if (types.contains(Context.Type.SHARED)
                && types.contains(Context.Type.INDEXED)) {
            cxt = new SharedIndexedContext(name);
        } else if (types.contains(Context.Type.SHARED)) {
            cxt = new SharedAssociativeContext(name);
        } else if (types.contains(Context.Type.ASSOCIATIVE)) {
            if (subject != null)
                cxt = new ServiceContext(name, subject.path(), subject.value());
            else
                cxt = new ServiceContext(name);
        } else {
            if (subject != null) {
                cxt = new PositionalContext(name, subject.path(),
                        subject.value());
            } else {
                cxt = new PositionalContext(name);
            }
        }
        if (cxt instanceof PositionalContext) {
            PositionalContext pcxt = (PositionalContext) cxt;
            if (entryList.size() > 0) {
                for (int i = 0; i < entryList.size(); i++) {
                    if (entryList.get(i) instanceof InEntry) {
                        pcxt.putInValueAt(((InEntry) entryList.get(i)).path(),
                                ((InEntry) entryList.get(i)).value(), i + 1);
                    } else if (entryList.get(i) instanceof OutEntry) {
                        pcxt.putOutValueAt(
                                ((OutEntry) entryList.get(i)).path(),
                                ((OutEntry) entryList.get(i)).value(), i + 1);
                    } else if (entryList.get(i) instanceof InoutEntry) {
                        pcxt.putInoutValueAt(
                                ((InoutEntry) entryList.get(i)).path(),
                                ((InoutEntry) entryList.get(i)).value(), i + 1);
                    } else if (entryList.get(i) instanceof Entry) {
                        pcxt.putValueAt(((Entry) entryList.get(i)).path(),
                                ((Entry) entryList.get(i)).value(), i + 1);
                    } else if (entryList.get(i) instanceof DataEntry) {
                        pcxt.putValueAt(Context.DSD_PATH,
                                ((DataEntry) entryList.get(i)).value(), i + 1);
                    } else if (entryList.get(i) instanceof Tuple2) {
                        pcxt.putValueAt(
                                entryList.get(i)._1,
                                entryList.get(i)._2,
                                i + 1);
                    }
                }
            }
        } else {
            if (entryList.size() > 0) {
                for (int i = 0; i < entryList.size(); i++) {
                    if (entryList.get(i) instanceof InEntry) {
                        cxt.putInValue(((Entry) entryList.get(i)).path(),
                                ((Entry) entryList.get(i)).value());
                    } else if (entryList.get(i) instanceof OutEntry) {
                        cxt.putOutValue(((Entry) entryList.get(i)).path(),
                                ((Entry) entryList.get(i)).value());
                    } else if (entryList.get(i) instanceof InoutEntry) {
                        cxt.putInoutValue(((Entry) entryList.get(i)).path(),
                                ((Entry) entryList.get(i)).value());
                    } else if (entryList.get(i) instanceof Entry) {
                        cxt.putValue(((Entry) entryList.get(i)).path(),
                                ((Entry) entryList.get(i)).value());
                    } else if (entryList.get(i) instanceof DataEntry) {
                        cxt.putValue(Context.DSD_PATH,
                                ((Entry) entryList.get(i)).value());
                    } else if (entryList.get(i) instanceof Tuple2) {
                        cxt.putValue(
                                entryList.get(i)._1,
                                entryList.get(i)._2);
                    }
                }
            }
        }

        if (returnPath != null)
            ((ServiceContext) cxt).setReturnPath(returnPath);
        if (cxtArgs != null) {
            if (cxtArgs.path() != null) {
                ((ServiceContext) cxt).setArgsPath(cxtArgs.path());
            } else {
                ((ServiceContext) cxt).setArgsPath(Context.PARAMETER_VALUES);
            }
            ((ServiceContext) cxt).setArgs(cxtArgs.args);
        }
        if (parameterTypes != null) {
            if (parameterTypes.path() != null) {
                ((ServiceContext) cxt).setParameterTypesPath(parameterTypes
                        .path());
            } else {
                ((ServiceContext) cxt)
                        .setParameterTypesPath(Context.PARAMETER_TYPES);
            }
            ((ServiceContext) cxt)
                    .setParameterTypes(parameterTypes.parameterTypes);
        }
        if (target != null) {
            if (target.path() != null) {
                ((ServiceContext) cxt).setTargetPath(target.path());
            }
            ((ServiceContext) cxt).setTarget(target.target);
        }
        return cxt;
    }

}
