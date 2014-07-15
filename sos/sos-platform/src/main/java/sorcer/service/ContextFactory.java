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


import sorcer.co.tuple.*;
import sorcer.core.context.ArrayContext;
import sorcer.core.context.ListContext;
import sorcer.core.context.PositionalContext;
import sorcer.core.context.ServiceContext;
import sorcer.core.context.SharedAssociativeContext;
import sorcer.core.context.SharedIndexedContext;
import sorcer.core.context.model.PoolStrategy;
import sorcer.core.context.model.par.Par;
import sorcer.core.context.model.par.ParImpl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static sorcer.util.UnknownName.getUnknown;

/**
 * Extracted from oe.operator by Rafał Krupiński
 */
public class ContextFactory {


    public static <T extends Object> Context context(T... entries)
            throws ContextException {
        if (entries[0] instanceof Exertion) {
            Exertion xrt = (Exertion) entries[0];
            if (entries.length >= 2 && entries[1] instanceof String)
                xrt = ((Job) xrt).getComponentExertion((String) entries[1]);
            return xrt.getContext();
        } else if (entries[0] instanceof String) {
            if (entries.length == 1)
                return new PositionalContext((String) entries[0]);
            else if (entries[1] instanceof Exertion) {
                return ((Job) entries[1]).getComponentExertion(
                        (String) entries[0]).getContext();
            }
        }
        String name = getUnknown();
        List<Tuple2<String, ?>> entryList = new ArrayList<Tuple2<String, ?>>();
        List<Par> parList = new ArrayList<Par>();
        List<Context.Type> types = new ArrayList<Context.Type>();
        List<EntryList> entryLists = new ArrayList<EntryList>();
        Complement subject = null;
        ReturnPath returnPath = null;
        ExecPath execPath = null;
        Args cxtArgs = null;
        ParameterTypes parameterTypes = null;
        target target = null;
        PoolStrategy modelStrategy = null;
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
            } else if (o instanceof ReturnPath) {
                returnPath = (ReturnPath) o;
            } else if (o instanceof ExecPath) {
                execPath = (ExecPath) o;
            } else if (o instanceof Tuple2) {
                entryList.add((Tuple2) o);
            } else if (o instanceof Context.Type) {
                types.add((Context.Type) o);
            } else if (o instanceof String) {
                name = (String) o;
            } else if (o instanceof PoolStrategy) {
                modelStrategy = (PoolStrategy) o;
            } else if (o instanceof Par) {
                parList.add((Par) o);
            } else if (o instanceof EntryList) {
                entryLists.add((EntryList) o);
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
            if (entryList.size() > 0)
                popultePositionalContext(pcxt, entryList);
        } else {
            if (entryList.size() > 0)
                populteContext(cxt, entryList);
        }
        if (parList != null) {
            for (Par p : parList)
                cxt.putValue(p.getName(), p);
        }
        if (returnPath != null)
            ((ServiceContext) cxt).setReturnPath(returnPath);
        if (execPath != null)
            ((ServiceContext) cxt).setExecPath(execPath);
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
        if (entryLists.size() > 0)
            ((ServiceContext)cxt).setEntryLists(entryLists);
        return cxt;
    }

    public static void popultePositionalContext(PositionalContext pcxt,
                                                   List<Tuple2<String, ?>> entryList) throws ContextException {
        for (int i = 0; i < entryList.size(); i++) {
            if (entryList.get(i) instanceof InEntry) {
                Object par = ((InEntry)entryList.get(i)).value();
                if (par instanceof Scopable) {
                    try {
                        ((Scopable)par).setScope(pcxt);
                    } catch (RemoteException e) {
                        throw new ContextException(e);
                    }
                }
                if (((InEntry) entryList.get(i)).isPersistant) {
                    setPar(pcxt, (InEntry) entryList.get(i), i);
                } else {
                    pcxt.putInValueAt(((InEntry) entryList.get(i)).path(),
                            ((InEntry) entryList.get(i)).value(), i + 1);
                }
            } else if (entryList.get(i) instanceof OutEntry) {
                if (((OutEntry) entryList.get(i)).isPersistant) {
                    setPar(pcxt, (OutEntry) entryList.get(i), i);
                } else {
                    pcxt.putOutValueAt(((OutEntry) entryList.get(i)).path(),
                            ((OutEntry) entryList.get(i)).value(), i + 1);
                }
            } else if (entryList.get(i) instanceof InoutEntry) {
                if (((InoutEntry) entryList.get(i)).isPersistant) {
                    setPar(pcxt, (InoutEntry) entryList.get(i), i);
                } else {
                    pcxt.putInoutValueAt(
                            ((InoutEntry) entryList.get(i)).path(),
                            ((InoutEntry) entryList.get(i)).value(), i + 1);
                }
            } else if (entryList.get(i) instanceof Entry) {
                if (((Entry) entryList.get(i)).isPersistant) {
                    setPar(pcxt, (Entry) entryList.get(i), i);
                } else {
                    pcxt.putValueAt(((Entry) entryList.get(i)).path(),
                            ((Entry) entryList.get(i)).value(), i + 1);
                }
            } else if (entryList.get(i) instanceof DataEntry) {
                pcxt.putValueAt(Context.DSD_PATH,
                        ((DataEntry) entryList.get(i)).value(), i + 1);
            }
        }
    }

    public static void populteContext(Context cxt,
                                         List<Tuple2<String, ?>> entryList) throws ContextException {
        for (int i = 0; i < entryList.size(); i++) {
            if (entryList.get(i) instanceof InEntry) {
                if (((InEntry) entryList.get(i)).isPersistant) {
                    setPar(cxt, (InEntry) entryList.get(i));
                } else {
                    cxt.putInValue(((Entry) entryList.get(i)).path(),
                            ((Entry) entryList.get(i)).value());
                }
            } else if (entryList.get(i) instanceof OutEntry) {
                if (((OutEntry) entryList.get(i)).isPersistant) {
                    setPar(cxt, (OutEntry) entryList.get(i));
                } else {
                    cxt.putOutValue(((Entry) entryList.get(i)).path(),
                            ((Entry) entryList.get(i)).value());
                }
            } else if (entryList.get(i) instanceof InoutEntry) {
                if (((InoutEntry) entryList.get(i)).isPersistant) {
                    setPar(cxt, (InoutEntry) entryList.get(i));
                } else {
                    cxt.putInoutValue(((Entry) entryList.get(i)).path(),
                            ((Entry) entryList.get(i)).value());
                }
            } else if (entryList.get(i) instanceof Entry) {
                if (((Entry) entryList.get(i)).isPersistant) {
                    setPar(cxt, (Entry) entryList.get(i));
                } else {
                    cxt.putValue(((Entry) entryList.get(i)).path(),
                            ((Entry) entryList.get(i)).value());
                }
            } else if (entryList.get(i) instanceof DataEntry) {
                cxt.putValue(Context.DSD_PATH,
                        ((Entry) entryList.get(i)).value());
            }
        }
    }

    public static void setPar(PositionalContext pcxt, Tuple2 entry, int i)
            throws ContextException {

        Par p = new ParImpl(entry.path(), entry.value());
        p.setPersistent(true);
        if (entry.datastoreURL != null)
            p.setDbURL(entry.datastoreURL);
        if (entry instanceof InEntry)
            pcxt.putInValueAt(entry.path(), p, i + 1);
        else if (entry instanceof OutEntry)
            pcxt.putOutValueAt(entry.path(), p, i + 1);
        else if (entry instanceof InoutEntry)
            pcxt.putInoutValueAt(entry.path(), p, i + 1);
        else
            pcxt.putValueAt(entry.path(), p, i + 1);
    }

    public static void setPar(Context cxt, Tuple2 entry)
            throws ContextException {

        Par p = new ParImpl(entry.path(), entry.value());
        p.setPersistent(true);
        if (entry.datastoreURL != null)
            p.setDbURL(entry.datastoreURL);
        if (entry instanceof InEntry)
            cxt.putInValue(entry.path(), p);
        else if (entry instanceof OutEntry)
            cxt.putOutValue(entry.path(), p);
        else if (entry instanceof InoutEntry)
            cxt.putInoutValue(entry.path(), p);
        else
            cxt.putValue(entry.path(), p);
    }
}
