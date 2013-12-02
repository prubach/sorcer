/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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
package sorcer.eo;

import static sorcer.eo.operator.store;
import static sorcer.util.UnknownName.getUnknown;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import sorcer.co.tuple.Entry;
import sorcer.co.tuple.ExecPath;
import sorcer.co.tuple.InEntry;
import sorcer.co.tuple.InoutEntry;
import sorcer.co.tuple.OutEntry;
import sorcer.co.tuple.Path;
import sorcer.co.tuple.Tuple2;
import sorcer.core.SorcerConstants;
import sorcer.core.context.*;
import sorcer.core.context.model.par.Par;
import sorcer.core.provider.Jobber;
import sorcer.core.provider.Provider;
import sorcer.core.provider.Spacer;
import sorcer.core.exertion.AltExertion;
import sorcer.core.exertion.LoopExertion;
import sorcer.core.exertion.NetJob;
import sorcer.core.exertion.NetTask;
import sorcer.core.exertion.ObjectJob;
import sorcer.core.exertion.ObjectTask;
import sorcer.core.exertion.OptExertion;
import sorcer.core.signature.EvaluationSignature;
import sorcer.core.signature.NetSignature;
import sorcer.core.signature.ObjectSignature;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.*;
import sorcer.service.Signature.Kind;
import sorcer.service.Signature.Type;
import sorcer.service.Strategy.Access;
import sorcer.service.Strategy.Flow;
import sorcer.service.Strategy.Monitor;
import sorcer.service.Strategy.Provision;
import sorcer.service.Strategy.Wait;
import sorcer.util.ServiceExerter;
import sorcer.util.Sorcer;
import sorcer.util.bdb.objects.Store;
import sorcer.util.bdb.sdb.DbpUtil;
import sorcer.util.bdb.sdb.SdbUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class operator {

	private static int count = 0;

	private static final Logger logger = Logger.getLogger(operator.class
			.getName());

	public static String path(List<String> attributes) {
		if (attributes.size() == 0)
			return null;
		if (attributes.size() > 1) {
			StringBuilder spr = new StringBuilder();
			for (int i = 0; i < attributes.size() - 1; i++) {
				spr.append(attributes.get(i)).append(SorcerConstants.CPS);
			}
			spr.append(attributes.get(attributes.size() - 1));
			return spr.toString();
		}
		return attributes.get(0);
	}

	public static Object revalue(Evaluation evaluation, String path,
			Arg... entries) throws ContextException {
		Object obj = value(evaluation, path, entries);
		if (obj instanceof Evaluation) {
			obj = value((Evaluation) obj, entries);
		}
		return obj;
	}

	public static Object revalue(Object object, Arg... entries)
			throws EvaluationException {
		Object obj = null;
		if (object instanceof Evaluation) {
			obj = value((Evaluation) object, entries);
		}
		if (obj == null) {
			obj = object;
		}
		return obj;
	}

	public static String path(String... attributes) {
		if (attributes.length == 0)
			return null;
		if (attributes.length > 1) {
			StringBuilder spr = new StringBuilder();
			for (int i = 0; i < attributes.length - 1; i++) {
				spr.append(attributes[i]).append(SorcerConstants.CPS);
			}
			spr.append(attributes[attributes.length - 1]);
			return spr.toString();
		}
		return attributes[0];
	}

	public static <T> Complement<T> subject(String path, T value)
			throws SignatureException {
		return new Complement<T>(path, value);
	}

	// TODO VFE related
	/*public static VarList put(VarList list, Tuple2... entries)
			throws VarException {
		list.setVarValues(entries);
		return list;
	} */

	public static <T extends Context> T put(T context, Tuple2... entries)
			throws ContextException {
		for (int i = 0; i < entries.length; i++) {
            // TODO VFE related
	        /*
			    if (context instanceof VarModel) {
				try {
					((VarModel) context).getVar(
							((Tuple2<String, ?>) entries[i])._1).setValue(
							((Tuple2<String, ?>) entries[i])._2);
				} catch (Exception e) {
					e.printStackTrace();
					throw new VarException(e);
				}
			} else */
			if (context instanceof Context) {
				context.putValue(((Tuple2<String, ?>) entries[i])._1,
						((Tuple2<String, ?>) entries[i])._2);
			}
		}
		return context;
	}

	public static void put(Exertion exertion, Tuple2<String, ?>... entries)
			throws ContextException {
		put(exertion.getContext(), entries);
	}

	public static Exertion setContext(Exertion exertion, Context context) {
		((ServiceExertion) exertion).setContext(context);
		return exertion;
	}

	public static ControlContext control(Exertion exertion)
			throws ContextException {
		return (ControlContext)exertion.getControlContext();
	}

	public static ControlContext control(Exertion exertion, String childName)
			throws ContextException {
		return (ControlContext)exertion.getExertion(childName).getControlContext();
	}

	public static <T extends Object> Context cxt(T... entries)
			throws ContextException {
		return context(entries);
	}

	public static Context jCxt(Job job) throws ContextException {
		return job.getJobContext();
	}

	public static Context jobContext(Exertion job) throws ContextException {
		return ((Job) job).getJobContext();
	}

	public static DataEntry data(Object data) {
		return new DataEntry(Context.DSD_PATH, data);
	}

	public static Context taskContext(String path, Job job) {
		return job.getComponentContext(path);
	}

/*    public static <T extends Object> Context context(T... entries)
            throws ContextException {
        return ContextFactory.context(entries);
    }*/

    // TODO VFE related
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
		/*VarInfoList nVarsInfoList = new VarInfoList();
		VarInfoList iVarsInfoList = null;
		VarInfoList oVarsInfoList = null;
		List<EntryList> entryLists = new ArrayList<EntryList>();*/
        Complement subject = null;
        ReturnPath returnPath = null;
        ExecPath execPath = null;
        Args cxtArgs = null;
		ParameterTypes parameterTypes = null;
		target target = null;
        //Table table = null;
        Strategy modelStrategy = null;
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
            } /* else if (o instanceof Table) {
				table = (Table) o;
			} else if (o instanceof Strategy) {
				modelStrategy = (Strategy) o;
			} else if (o instanceof VarInfo) {
				nVarsInfoList.add((VarInfo) o);
			}*/ else if (o instanceof Par) {
                parList.add((Par) o);
            } /*else if (o instanceof EntryList) {
				entryLists.add((EntryList) o);
			} else if (o instanceof VarInfoList && ((VarInfoList) o).size() > 0) {
				if (((VarInfoList) o).get(0).getType() == Variability.Type.INPUT) {
					iVarsInfoList = (VarInfoList) o;
				} else if (((VarInfoList) o).get(0).getType() == Variability.Type.OUTPUT) {
					oVarsInfoList = (VarInfoList) o;
				}
			} */
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
                ContextFactory.popultePositionalContext(pcxt, entryList);
        } else {
            if (entryList.size() > 0)
                ContextFactory.populteContext(cxt, entryList);
        }
        if (parList != null) {
            for (Par p : parList)
                cxt.putValue(p.getName(), p);
        }
        // parametric context items
		/*if (table != null)
			cxt.putValue(Modeling.IN_TABLE, table);
		if (modelStrategy != null)
			cxt.putValue(Modeling.MODEL_STRATEGY, modelStrategy);
		if (nVarsInfoList.size() > 0) {
			cxt.putValue(ResponseContext.NONE_INFO, nVarsInfoList);
		}
		if (iVarsInfoList != null && iVarsInfoList.size() > 0) {
			cxt.putValue(ResponseContext.INPUTS_INFO, iVarsInfoList);
		}
		if (oVarsInfoList != null && oVarsInfoList.size() > 0) {
			cxt.putValue(ResponseContext.OUTPUTS_INFO, oVarsInfoList);
		} */
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
		/*if (entryLists.size() > 0)
			((ServiceContext)cxt).setEntryLists(entryLists);*/
        return cxt;
    }

	public static List<String> names(List<? extends Identifiable> list) {
		List<String> names = new ArrayList<String>(list.size());
		for (Identifiable i : list) {
			names.add(i.getName());
		}
		return names;
	}

	public static String name(Object identifiable) {
		if (identifiable instanceof Identifiable)
			return ((Identifiable) identifiable).getName();
		else
			return null;
	}

	public static List<String> names(Identifiable... array) {
		List<String> names = new ArrayList<String>(array.length);
		for (Identifiable i : array) {
			names.add(i.getName());
		}
		return names;
	}

	public static List<Entry> attributes(Entry... entries) {
		List<Entry> el = new ArrayList<Entry>(entries.length);
		for (Entry e : entries)
			el.add(e);
		return el;
	}

	/**
	 * Makes this Revaluation revaluable, so its return value is to be again
	 * evaluated as well.
	 * 
	 * @param var
	 *            to be marked as revaluable
	 * @return an uevaluable Evaluation
	 * @throws EvaluationException
	 */
	public static Revaluation revaluable(Revaluation evaluation, Arg... entries)
			throws EvaluationException {
		if (entries != null && entries.length > 0) {
			try {
				((Evaluation) evaluation).substitute(entries);
			} catch (RemoteException e) {
				throw new EvaluationException(e);
			}
		}
		evaluation.setRevaluable(true);
		return evaluation;
	}

    /*public static Signature sig(String operation, Class<?> serviceType,
                                List<net.jini.core.entry.Entry> attributes)
            throws SignatureException {
        return SignatureFactory.sig(operation, serviceType, attributes);
    } */

	public static Revaluation unrevaluable(Revaluation evaluation) {
		evaluation.setRevaluable(false);
		return evaluation;
	}

	/**
	 * Returns the Evaluation with a realized substitution for its arguments.
	 * 
	 * @param evaluation
	 * @param entries
	 * @return an evaluation with a realized substitution
	 * @throws EvaluationException
	 * @throws RemoteException
	 */
	public static Evaluation substitute(Evaluation evaluation, Arg... entries)
			throws EvaluationException, RemoteException {
		return evaluation.substitute(entries);
	}

    public static Signature sig(Class<?> serviceType, ReturnPath returnPath)
            throws SignatureException {
        return SignatureFactory.sig(serviceType, returnPath);
    }
	public static Signature sig(Class<?> serviceType, String providerName,
			Object... parameters) throws SignatureException {
		return sig(null, serviceType, Sorcer.getActualName(providerName),
				parameters);
	}

	public static Signature sig(String operation, Class<?> serviceType,
			String providerName, Object... parameters)
			throws SignatureException {
		return SignatureFactory.sig(operation, serviceType, providerName, parameters);
        /*#SOURCE sig_operation_servicetype_providername_parameters
        Signature sig = null;
        if (serviceType.isInterface()) {
            sig = new NetSignature(operation, serviceType,
                    Sorcer.getActualName(providerName));
        } else {
            sig = new ObjectSignature(operation, serviceType);
        }
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
        #*/
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
			Provision type) throws SignatureException {
		return sig(operation, serviceType, (String) null, type);
	}

	public static Signature sig(String operation, Class<?> serviceType,
			List<net.jini.core.entry.Entry> attributes)
			throws SignatureException {
		NetSignature op = new NetSignature();
		op.setAttributes(attributes);
		op.setServiceType(serviceType);
		op.setSelector(operation);
		return op;
	}

	public static Signature sig(Class<?> serviceType) throws SignatureException {
		return SignatureFactory.sig(serviceType, (ReturnPath) null);
/*#SOURCE sig_class_returnpath
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
#*/

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

    public static <T> Task task(String name, T... elems)
            throws ExertionException {
        return TaskFactory.task(name, elems);
    }

	public static String selector(Signature sig) {
		return sig.getSelector();
	}

    public static Signature type(Signature signature, Signature.Type type) {
        signature.setType(type);
        return signature;
    }
    // TODO VFE related
    /*
    public static EvaluationSignature sig(ServiceEvaluator evaluator)
			throws SignatureException {
		return new EvaluationSignature(evaluator);
	}

	public static EvaluationSignature sig(ServiceEvaluator evaluator,
			ReturnPath returnPath) throws SignatureException {
		EvaluationSignature sig = new EvaluationSignature(evaluator);
		sig.setReturnPath(returnPath);
		return sig;
	}

	public static EvaluationTask task(EvaluationSignature signature)
			throws ExertionException {
		return new EvaluationTask(signature);
	}

	public static EvaluationTask task(EvaluationSignature signature,
			Context context) throws ExertionException {
		return new EvaluationTask(signature, context);
	}

	public static FilterSignature sig(Filter filter) throws SignatureException {
		return new FilterSignature(null, filter);
	}

	public static FilterSignature sig(Object paramter, Filter filter)
			throws SignatureException {
		return new FilterSignature(paramter, filter);
	}
	*/

	public static ObjectSignature sig(String operation, Object object,
			Class... types) throws SignatureException {
		try {
			if (object instanceof Class && ((Class) object).isInterface()) {
				return new NetSignature(operation, (Class) object);
			} else {
				return new ObjectSignature(operation, object,
						types.length == 0 ? null : types);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SignatureException(e);
		}
	}

	public static ObjectSignature sig(String selector, Object object,
			Class<?>[] types, Object[] args) throws SignatureException {
		try {
			return new ObjectSignature(selector, object, types, args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SignatureException(e);
		}
	}

	public static ObjectTask task(ObjectSignature signature)
			throws SignatureException {
		return new ObjectTask(signature.getSelector(),
				(ObjectSignature) signature);
	}

	public static ObjectTask task(ObjectSignature signature, Context context)
			throws SignatureException {
		return new ObjectTask(signature.getSelector(),
				(ObjectSignature) signature, context);
	}

    // TODO VFE related
    public static Task task(String name, Signature signature, Context context)
			throws SignatureException {
		if (signature instanceof NetSignature) {
			return new NetTask(name, (NetSignature) signature, context);
		} else if (signature instanceof ObjectSignature) {
			return new ObjectTask(name, (ObjectSignature) signature, context);
		} /*else if (signature instanceof EvaluationSignature) {
			return new EvaluationTask(name, (EvaluationSignature) signature,
					context);
		} else if (signature instanceof VarSignature) {
			return new VarTask(name, (VarSignature) signature, context);
		} else if (signature instanceof FilterSignature) {
			return new FilterTask(name, (FilterSignature) signature, context);
		} */
          else
			return new Task(name, signature, context);
	}

    // TODO VFE related
	public static Task task(Signature signature, Context context)
			throws SignatureException {
		if (signature instanceof NetSignature) {
			return new NetTask(null, (NetSignature) signature, context);
		} else if (signature instanceof ObjectSignature) {
			return new ObjectTask(null, (ObjectSignature) signature, context);
		} /* else if (signature instanceof EvaluationSignature) {
			return new EvaluationTask((EvaluationSignature) signature, context);
		} else if (signature instanceof VarSignature) {
			return new VarTask((VarSignature) signature, context);
		} else if (signature instanceof FilterSignature) {
			return new FilterTask((FilterSignature) signature, context);
		} */
          else
			return new Task(null, signature, context);
	}

	public static <T> Task batch(String name, T... elems)
			throws ExertionException {
		Task batch = task(name, elems);
		if (batch.getSignatures().size() > 1)
			return batch;
		else
			throw new ExertionException(
					"A batch should comprise of more than one signature.");
	}

	public static <T extends Object, E extends Exertion> E srv(String name,
			T... elems) throws ExertionException, ContextException,
			SignatureException {
		return (E) exertion(name, elems);
	}

	public static <T extends Object, E extends Exertion> E xrt(String name,
			T... elems) throws ExertionException, ContextException,
			SignatureException {
		return (E) exertion(name, elems);
	}

	public static <T extends Object, E extends Exertion> E exertion(
			String name, T... elems) throws ExertionException,
			ContextException, SignatureException {
		List<Exertion> exertions = new ArrayList<Exertion>();
		for (int i = 0; i < elems.length; i++) {
			if (elems[i] instanceof Exertion) {
				exertions.add((Exertion) elems[i]);
			}
		}
		if (exertions.size() > 0) {
			Job j = job(elems);
			j.setName(name);
			return (E) j;
		} else {
			Task t = task(name, elems);
			return (E) t;
		}
	}

	public static <T> Job job(T... elems) throws ExertionException,
			ContextException, SignatureException {
		String name = getUnknown();
		Signature signature = null;
		ControlContext control = null;
		Context<?> data = null;
		ReturnPath rp = null;
		List<Exertion> exertions = new ArrayList<Exertion>();
		List<Pipe> pipes = new ArrayList<Pipe>();

		for (int i = 0; i < elems.length; i++) {
			if (elems[i] instanceof String) {
				name = (String) elems[i];
			} else if (elems[i] instanceof Exertion) {
				exertions.add((Exertion) elems[i]);
			} else if (elems[i] instanceof ControlContext) {
				control = (ControlContext) elems[i];
			} else if (elems[i] instanceof Context) {
				data = (Context<?>) elems[i];
			} else if (elems[i] instanceof Pipe) {
				pipes.add((Pipe) elems[i]);
			} else if (elems[i] instanceof Signature) {
				signature = ((Signature) elems[i]);
			} else if (elems[i] instanceof ReturnPath) {
				rp = ((ReturnPath) elems[i]);
			}
		}

		Job job = null;
		if (signature == null) {
			signature = sig("service", Jobber.class);
		}
		if (signature instanceof NetSignature) {
			job = new NetJob(name);
		} else if (signature instanceof ObjectSignature) {
			job = new ObjectJob(name);
		}
		job.addSignature(signature);
		if (data != null)
			job.setContext(data);

		if (rp != null) {
			((ServiceContext) job.getDataContext()).setReturnJobPath(rp);
		}

		if (job instanceof NetJob && control != null) {
			job.setControlContext(control);
			if (control.getAccessType().equals(Access.PULL)) {
				Signature procSig = job.getProcessSignature();
				procSig.setServiceType(Spacer.class);
				job.getSignatures().clear();
				job.addSignature(procSig);
				if (data != null)
					job.setContext(data);
				else
					job.getDataContext().setExertion(job);
			}
		}
		if (exertions.size() > 0) {
			for (Exertion ex : exertions) {
				job.addExertion(ex);
			}
			for (Pipe p : pipes) {
				logger.finer("from context: "
						+ ((Exertion) p.in).getDataContext().getName()
						+ " path: " + p.inPath);
				logger.finer("to context: "
						+ ((Exertion) p.out).getDataContext().getName()
						+ " path: " + p.outPath);
				((Exertion) p.out).getDataContext().connect(p.outPath,
						p.inPath, ((Exertion) p.in).getContext());
			}
		} else
			throw new ExertionException("No component exertion defined.");

		return job;
	}

	public static Object get(Context context) throws ContextException,
			RemoteException {
		return ((ServiceContext) context).getReturnValue();
	}

	public static Object get(Context context, int index)
			throws ContextException {
		if (context instanceof PositionalContext)
			return ((PositionalContext) context).getValueAt(index);
		else
			throw new ContextException("Not PositionalContext, index: " + index);
	}

	public static Object get(Exertion exertion) throws ContextException,
			RemoteException {
		return ((ServiceContext) exertion.getContext()).getReturnValue();
	}

	public static <V> V asis(Object evaluation) throws EvaluationException {
		if (evaluation instanceof Evaluation) {
			try {
				synchronized (evaluation) {
					return ((Evaluation<V>) evaluation).asis();
				}
			} catch (RemoteException e) {
				throw new EvaluationException(e);
			}
		} else {
			throw new EvaluationException(
					"asis value can only be determined for objects of the "
							+ Evaluation.class + " type");
		}
	}

    // TODO VFE related
    /*
	public static <V> V take(Variability<V> variability)
			throws EvaluationException {
		try {
			synchronized (variability) {
				variability.valueChanged(null);
				V val = variability.getValue();
				variability.valueChanged(null);
				return val;
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	} */

	public static Object get(Exertion exertion, String component, String path)
			throws ExertionException {
		Exertion c = ((Exertion) exertion).getExertion(component);
		return get((Exertion) c, path);
	}

	public static Object value(URL url) throws IOException {
		return url.getContent();
	}

	public static <T> T value(Evaluation<T> evaluation, Arg... entries)
			throws EvaluationException {
		try {
			synchronized (evaluation) {
				/* if (evaluation instanceof ParModel) {
					return (T) ((ParModel) evaluation).getValue(entries);
				} else */ if (evaluation instanceof Exertion) {
					return (T) execExertion((Exertion) evaluation, null,
							entries);
				} else {
					return evaluation.getValue(entries);
				}
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public static <T> T value(Evaluation<T> evaluation, String evalSelector,
			Arg... entries) throws EvaluationException {
		/* if (evaluation instanceof ParModel) {
			try {
				return (T) ((ParModel) evaluation).getValue(evalSelector,
						entries);
			} catch (ContextException e) {
				throw new EvaluationException(e);
			}
		} else if (evaluation instanceof Var<?>) {
			((Var<T>) evaluation).selectFidelity(evalSelector);
			return (T) value(evaluation, entries);
		} else */ if (evaluation instanceof Exertion) {
			try {
				return (T) execExertion((Exertion) evaluation, evalSelector,
						entries);
			} catch (Exception e) {
				e.printStackTrace();
				throw new EvaluationException(e);
			}
		} else if (evaluation instanceof Context) {
			try {
				return (T) ((Context) evaluation).getValue(evalSelector,
						entries);
			} catch (Exception e) {
				e.printStackTrace();
				throw new EvaluationException(e);
			}
		}
		return null;
	}

	public static Object url(Context model, String name)
			throws ContextException, RemoteException {
		return ((ServiceContext) model).getURL(name);
	}

	public static Object asis(Mappable mappable, String path)
			throws ContextException {
		return ((ServiceContext) mappable).asis(path);
	}

	public static Object get(Mappable mappable, String path)
			throws ContextException {
		Object obj = ((ServiceContext) mappable).asis(path);
        while (obj instanceof Mappable || obj instanceof Par) {
			try {
				obj = ((Evaluation) obj).asis();
			} catch (RemoteException e) {
				throw new ContextException(e);
			}
		}
		return obj;
	}

	public static List<Exertion> exertions(Exertion xrt) {
		return xrt.getAllExertions();
	}

	public static Exertion exertion(Exertion xrt, String componentExertionName) {
		return ((Job) xrt).getComponentExertion(componentExertionName);
	}

	public static List<String> trace(Exertion xrt) {
		return ((ControlContext)xrt.getControlContext()).getTrace();
	}

	public static void print(Object obj) {
		System.out.println(obj.toString());
	}

	public static Object exec(Context context, Arg... entries)
			throws ExertionException, ContextException {
		try {
			context.substitute(entries);
		} catch (RemoteException e) {
			throw new ContextException(e);
		}
		ReturnPath returnPath = context.getReturnPath();
		if (returnPath != null) {
			return context.getValue(returnPath.path, entries);
		} else
			throw new ExertionException("No return path in the context: "
					+ context.getName());
	}

	public static Object execExertion(Exertion exertion, String path,
			Arg... entries) throws ExertionException, ContextException,
			RemoteException {
		Exertion xrt;
		try {
			if (exertion.getClass() == Task.class) {
				if (((Task) exertion).getInnerTask() != null)
					xrt = exert(((Task) exertion).getInnerTask(), null, entries);
				else
					xrt = exertOpenTask(exertion, entries);
			} else {
				xrt = exert(exertion, null, entries);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExertionException(e);
		}

		Object obj = ((ServiceExertion) xrt).getReturnValue(entries);
		if (obj == null) {
			ReturnPath returnPath = xrt.getContext().getReturnPath();
			if (returnPath != null) {
				obj = ((ServiceExertion) xrt).getReturnValue(entries);
			}
		} else if (obj instanceof Context && path != null) {
			obj = ((Context) obj).getValue(path, entries);
		}

		return obj;
	}

	public static Exertion exertOpenTask(Exertion exertion, Arg... entries)
			throws ExertionException {
		Exertion closedTask = null;
		List<Arg> params = Arrays.asList(entries);
		List<Object> items = new ArrayList<Object>();
		for (Arg param : params) {
			if (param instanceof ControlContext
					&& ((ControlContext) param).getSignatures().size() > 0) {
				List<Signature> sigs = ((ControlContext) param).getSignatures();
				ControlContext cc = (ControlContext) param;
				cc.setSignatures(null);
				Context tc = exertion.getContext();
				items.add(tc);
				items.add(cc);
				items.addAll(sigs);
				closedTask = task(exertion.getName(), items.toArray());
			}
		}
		try {
			closedTask = closedTask.exert(entries);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExertionException(e);
		}
		return closedTask;
	}

	public static Object get(Exertion xrt, String path)
			throws ExertionException {
		try {
			return ((ServiceExertion) xrt).getValue(path);
		} catch (ContextException e) {
			throw new ExertionException(e);
		}
	}

    // TODO VFE related
	/*public static Object get(VarModel model, String varName)
			throws EvaluationException {
		try {
			return model.getVar(varName).getValue();
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	} */

	public static List<ThrowableTrace> exceptions(Exertion exertion) {
		return exertion.getExceptions();
	}

	public static Task exert(Task input, Entry... entries)
			throws ExertionException {
		try {
			return (Task) ((Task) input).exert(null, entries);
		} catch (Exception e) {
			throw new ExertionException(e);
		}
	}

	public static <T extends Exertion> T exert(Exertion input, Arg... entries)
			throws ExertionException {
		try {
			return (T) exert(input, null, entries);
		} catch (Exception e) {
			throw new ExertionException(e);
		}
	}

	public static <T extends Exertion> T exert(T input,
			Transaction transaction, Arg... entries) throws ExertionException {
		try {
			ServiceExerter esh = new ServiceExerter(input);
			Exertion result = null;
			try {
				result = esh.exert(transaction, null, entries);
			} catch (Exception e) {
				e.printStackTrace();
				if (result != null)
					((ServiceExertion) result).reportException(e);
			}
			return (T) result;
		} catch (Exception e) {
			throw new ExertionException(e);
		}
	}

	public static OutEntry output(Object value) {
		return new OutEntry(null, value, 0);
	}

	public static ReturnPath self() {
		return new ReturnPath();
	}

	public static ReturnPath result(String path, String... paths) {
		return new ReturnPath(path, paths);
	}

	public static ReturnPath result(String path, Direction direction,
			String... paths) {
		return new ReturnPath(path, direction, paths);
	}

	public static ReturnPath result(String path, Class type, String... paths) {
		return new ReturnPath(path, Direction.OUT, type, paths);
	}

	public static OutEntry output(String path, Object value) {
		return new OutEntry(path, value, 0);
	}

	public static OutEntry out(String path, Object value) {
		return new OutEntry(path, value, 0);
	}

    // TODO VFE related
    /*public static OutEntry entry(String path, FidelityInfo fidelity) {
		return new OutEntry(path, fidelity);
	}

	public static OutEntry out(String path, FidelityInfo fidelity) {
		return new OutEntry(path, fidelity);
	}*/

	public static OutEndPoint output(Exertion outExertion, String outPath) {
		return new OutEndPoint(outExertion, outPath);
	}

	public static OutEndPoint out(Mappable outExertion, String outPath) {
		return new OutEndPoint(outExertion, outPath);
	}

	public static InEndPoint input(Mappable inExertion, String inPath) {
		return new InEndPoint(inExertion, inPath);
	}

	public static InEndPoint in(Exertion inExertion, String inPath) {
		return new InEndPoint(inExertion, inPath);
	}

	public static OutEntry output(String path, Object value, int index) {
		return new OutEntry(path, value, index);
	}

	public static OutEntry out(String path, Object value, int index) {
		return new OutEntry(path, value, index);
	}

	public static OutEntry dbOutput(String path, Object value) {
		return new OutEntry(path, value, true, 0);
	}

	public static OutEntry dbOut(String path, Object value) {
		return new OutEntry(path, value, true, 0);
	}

	public static OutEntry dbOutput(String path, Object value, URL datasoreURL) {
		return new OutEntry(path, value, true, datasoreURL, 0);
	}

	public static OutEntry dbOut(String path, Object value, URL datasoreURL) {
		return new OutEntry(path, value, true, datasoreURL, 0);
	}

	public static InEntry input(String path) {
		return new InEntry(path, null, 0);
	}

	public static OutEntry out(String path) {
		return new OutEntry(path, null, 0);
	}

	public static OutEntry output(String path) {
		return new OutEntry(path, null, 0);
	}

	public static InEntry in(String path) {
		return new InEntry(path, null, 0);
	}

    // TODO VFE related
    /*public static InEntry input(Var var) {
		return new InEntry(var.getName(), var, 0);
	}

	public static InEntry in(Var var) {
		return input(var);
	}*/

	public static Entry at(String path, Object value) {
		return new Entry(path, value, 0);
	}

	public static Entry at(String path, Object value, int index) {
		return new Entry(path, value, index);
	}

	public static InEntry input(String path, Object value) {
		return new InEntry(path, value, 0);
	}

	public static InEntry in(String path, Object value) {
		return new InEntry(path, value, 0);
	}

	public static InEntry dbInput(String path, Object value) {
		return new InEntry(path, value, true, 0);
	}

	public static InEntry dbIn(String path, Object value) {
		return new InEntry(path, value, true, 0);
	}

	public static InEntry dbIntput(String path, Object value, URL datasoreURL) {
		return new InEntry(path, value, true, datasoreURL, 0);
	}

	public static InEntry dbIn(String path, Object value, URL datasoreURL) {
		return new InEntry(path, value, true, datasoreURL, 0);
	}

	public static InEntry input(String path, Object value, int index) {
		return new InEntry(path, value, index);
	}

	public static InEntry in(String path, Object value, int index) {
		return new InEntry(path, value, index);
	}

	public static InEntry inout(String path) {
		return new InEntry(path, null, 0);
	}

	public static InEntry inout(String path, Object value) {
		return new InEntry(path, value, 0);
	}

	public static InoutEntry inout(String path, Object value, int index) {
		return new InoutEntry(path, value, index);
	}

	/*private static String getUnknown() {
		return "unknown" + count++;
	} */

    // TODO VFE related
    /*public static class OutTable<T1, T2> extends Tuple2<T1, T2> {
		private static final long serialVersionUID = 1L;
		public Vars[] allNones = new Vars[] { Vars.NULL };
		public VarInfoList outVarsInfoList; // responses

		public OutTable(T1 location, T2 delimiter, Vars... allNones) {
			this(location, delimiter, null, allNones);
		}

		public OutTable(T1 location, VarInfoList inVarsInfo) {
			this(location, (T2) " ", inVarsInfo, new Vars[] { Vars.NULL });
		}

		public OutTable(T1 location, T2 delimiter, VarInfoList inVarsInfoList) {
			this(location, delimiter, inVarsInfoList, new Vars[] { Vars.NULL });
		}

		public OutTable(T1 location, T2 value, VarInfoList varsInfoList,
				Vars... allNones) {
			T2 v = value;
			if (v == null)
				v = (T2) Context.none;

			this._1 = location;
			this._2 = v;
			if (allNones != null)
				this.allNones = allNones;
			this.outVarsInfoList = varsInfoList;
		}

		public String toString() {
			return "location: "
					+ _1
					+ " delimiter: "
					+ _2
					+ "\noutputs: "
					+ (outVarsInfoList == null ? "" : outVarsInfoList
							.getNames());
		}
	}*/

	public static class Range extends Tuple2<Integer, Integer> {
		private static final long serialVersionUID = 1L;
		public Integer[] range;

		public Range(Integer from, Integer to) {
			this._1 = from;
			this._2 = to;
		}

		public Range(Integer[] range) {
			this.range = range;
		}

		public Integer[] range() {
			return range;
		}

		public int from() {
			return _1;
		}

		public int to() {
			return _2;
		}

		public String toString() {
			if (range != null)
				return Arrays.toString(range);
			else
				return "[" + _1 + "-" + _2 + "]";
		}
	}
    public static class Pipe {
		String inPath;
		String outPath;
		Mappable in;
		Mappable out;
		Par par;

		Pipe(Exertion out, String outPath, Mappable in, String inPath) {
			this.out = out;
			this.outPath = outPath;
			this.in = in;
			this.inPath = inPath;
			if ((in instanceof Exertion) && (out instanceof Exertion)) {
                par = new Par(outPath, inPath, in);
				((ServiceExertion) out).addPersister(par);
			}
		}

		Pipe(OutEndPoint outEndPoint, InEndPoint inEndPoint) {
			this.out = outEndPoint.out;
			this.outPath = outEndPoint.outPath;
			this.in = inEndPoint.in;
			this.inPath = inEndPoint.inPath;
			if ((in instanceof Exertion) && (out instanceof Exertion)) {
                par = new Par(outPath, inPath, in);
				((ServiceExertion) out).addPersister(par);
			}
		}
	}

    public static Par persistent(Pipe pipe) {
		pipe.par.setPersistent(true);
		return pipe.par;
	}

	public static Pipe pipe(OutEndPoint outEndPoint, InEndPoint inEndPoint) {
		Pipe p = new Pipe(outEndPoint, inEndPoint);
		return p;
	}

	// putLink(String name, String path, Context linkedContext, String offset)
	public static Object link(Context context, String path,
			Context linkedContext, String offset) throws ContextException {
		context.putLink(null, path, linkedContext, offset);
		return context;
	}

	public static Object link(Context context, String path,
			Context linkedContext) throws ContextException {
		context.putLink(null, path, linkedContext, "");
		return context;
	}

	public static <T> ControlContext strategy(T... entries) {
		ControlContext cc = new ControlContext();
		List<Signature> sl = new ArrayList<Signature>();
		for (Object o : entries) {
			if (o instanceof Access) {
				cc.setAccessType((Access) o);
			} else if (o instanceof Flow) {
				cc.setFlowType((Flow) o);
			} else if (o instanceof Monitor) {
				cc.isMonitorable((Monitor) o);
			} else if (o instanceof Provision) {
				cc.isProvisionable((Provision) o);
			} else if (o instanceof Wait) {
				cc.isWait((Wait) o);
			} else if (o instanceof Signature) {
				sl.add((Signature) o);
//			} else if (o instanceof Opti) {
//				cc.setOpti((Opti)o);
			}
		}
		cc.setSignatures(sl);
		return cc;
	}
    // TODO VFE related
	/*public static EntryList initialDesign(Entry...  entries) {
		EntryList el = new EntryList(entries);
		el.setType(EntryList.Type.INITIAL_DESIGN);
		return el;
	}*/
	
	public static URL dbURL() throws MalformedURLException {
		return new URL(Sorcer.getDatabaseStorerUrl());
	}

	public static URL dsURL() throws MalformedURLException {
		return new URL(Sorcer.getDataspaceStorerUrl());
	}

	public static void dbURL(Object object, URL dbUrl)
			throws MalformedURLException {
		if (object instanceof Par)
			((Par) object).setDbURL(dbUrl);
		else if (object instanceof ServiceContext)
			((ServiceContext) object).setDbUrl("" + dbUrl);
		else
			throw new MalformedURLException("Can not set URL to: " + object);
	}

	public static URL dbURL(Object object) throws MalformedURLException {
		if (object instanceof Par)
			return ((Par) object).getDbURL();
		else if (object instanceof ServiceContext)
			return new URL(((ServiceContext) object).getDbUrl());
		return null;
	}

    // TODO VFE related
    /*public static URL url(Var var) {
		if (var.getFilter() instanceof UrlFilter) {
			return ((UrlFilter) var.getFilter()).getURL();
		} else
			return null;
	}
*/
	public static URL store(Object object) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.store(object);
	}

	public static Object retrieve(URL url) throws IOException {
		return url.getContent();
	}

	public static URL update(Object object) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.update(object);
	}

	public static List<String> list(URL url) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.list(url);
	}

	public static List<String> list(Store store) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.list(store);
	}

	public static URL delete(Object object) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.delete(object);
	}

	public static int clear(Store type) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.clear(type);
	}

	public static int size(Store type) throws ExertionException,
			SignatureException, ContextException {
		return DbpUtil.size(type);
	}



    private static class InEndPoint {
		String inPath;
		Mappable in;

		InEndPoint(Mappable in, String inPath) {
			this.inPath = inPath;
			this.in = in;
		}
	}

	private static class OutEndPoint {
		public String outPath;
		public Mappable out;

		OutEndPoint(Mappable out, String outPath) {
			this.outPath = outPath;
			this.out = out;
		}
	}

	public static Object target(Object object) {
		return new target(object);
	}

	public static class target extends Path {
		private static final long serialVersionUID = 1L;
		Object target;

		target(Object target) {
			this.target = target;
		}

		target(String path, Object target) {
			this.target = target;
			this._1 = path;
		}

		@Override
		public String toString() {
			return "target: " + target;
		}
	}

	public static class result extends Tuple2 {

		private static final long serialVersionUID = 1L;

		Class returnType;

		result(String path) {
			this._1 = path;
		}

		result(String path, Class returnType) {
			this._1 = path;
			this._2 = returnType;
		}

		public Class returnPath() {
			return (Class) this._2;
		}

		@Override
		public String toString() {
			return "return path: " + _1;
		}
	}

	public static ParameterTypes parameterTypes(Class... parameterTypes) {
		return new ParameterTypes(parameterTypes);
	}

	public static class ParameterTypes extends Path {
		private static final long serialVersionUID = 1L;
		Class[] parameterTypes;

		public ParameterTypes(Class... parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		public ParameterTypes(String path, Class... parameterTypes) {
			this.parameterTypes = parameterTypes;
			this._1 = path;
		}

		@Override
		public String toString() {
			return "parameterTypes: " + Arrays.toString(parameterTypes);
		}
	}

	public static Args parameterValues(Object... args) {
		return new Args(args);
	}

	public static Args args(Object... args) {
		return new Args(args);
	}

	public static Args args(String path, Object... args) {
		return new Args(path, args);
	}

	public static class Args extends Path {
		private static final long serialVersionUID = 1L;

		Object[] args;

		public Args(Object... args) {
			this.args = args;
		}

		public Args(String path, Object... args) {
			this.args = args;
			this._1 = path;
		}

		@Override
		public String toString() {
			return "args: " + Arrays.toString(args);
		}
	}

	public static class DataEntry<T2> extends Tuple2<String, T2> {
		private static final long serialVersionUID = 1L;

		DataEntry(String path, T2 value) {
			T2 v = value;
			if (v == null)
				v = (T2) Context.none;

			this._1 = path;
			this._2 = v;
		}
	}

    // TODO VFE related
	/*public static class InTable<T1, T2> extends Tuple2<T1, T2> {
		private static final long serialVersionUID = 1L;

		Range range;
		public VarInfoList inVarsInfoList; // parameters
		public Vars[] allNones = new Vars[] { Vars.NULL };
		public Cell type = Cell.DOUBLE;

		public InTable(T1 path, T2 value) {
			T2 v = value;
			if (v == null)
				v = (T2) Vars.NULL;

			this._1 = path; // table source
			this._2 = v; // delimiter
		}

		public InTable(T1 source, T2 delimiter, Range range) {
			this(source, delimiter, range, null, Vars.NULL);
		}

		public InTable(T1 source, T2 delimiter, Range range,
				VarInfoList parameters) {
			this(source, delimiter, range, parameters, Vars.NULL);
		}

		public InTable(T1 source, T2 delimiter, Range range,
				VarInfoList parameters, Vars... allNones) {
			this(source, delimiter);
			this.range = range;
			inVarsInfoList = parameters;
			if (allNones != null)
				this.allNones = allNones;
		}

		public void setType(Cell type) {
			this.type = type;
		}

		public String getSource() {
			return (String) _1;
		}

		public String getDelimiter() {
			return (String) _2;
		}

		public Range getRange() {
			return range;
		}

		public String toString() {
			return "location: " + _1 + " delimiter: " + _2 + " range: " + range;
		}

	}*/

	public static class Complement<T2> extends Entry<T2> {
		private static final long serialVersionUID = 1L;

		Complement(String path, T2 value) {
			this._1 = path;
			this._2 = value;
		}
	}

	public static List<Service> providers(Signature signature)
			throws SignatureException {
		ServiceTemplate st = new ServiceTemplate(null,
				new Class[] { signature.getServiceType() }, null);
		ServiceItem[] sis = Accessor.getServiceItems(st, null,
				Sorcer.getLookupGroups());
		if (sis == null)
			throw new SignatureException("No available providers of type: "
					+ signature.getServiceType().getName());
		List<Service> servicers = new ArrayList<Service>(sis.length);
		for (ServiceItem si : sis) {
			servicers.add((Service) si.service);
		}
		return servicers;
	}

	public static List<Class<?>> interfaces(Object obj) {
		if (obj == null)
			return null;
		return Arrays.asList(obj.getClass().getInterfaces());
	}

	public static Object provider(Signature signature)
			throws SignatureException {
		Object target = null;
		Service provider = null;
		Class<?> providerType = null;
		if (signature instanceof NetSignature) {
			providerType = ((NetSignature) signature).getServiceType();
		} else if (signature instanceof ObjectSignature) {
			providerType = ((ObjectSignature) signature).getProviderType();
			target = ((ObjectSignature) signature).getTarget();
		}
		try {
			if (signature instanceof NetSignature) {
				provider = ((NetSignature) signature).getService();
				if (provider == null) {
					provider = (Service)Accessor.getService(signature);
					((NetSignature) signature).setProvider(provider);
				}
			} else if (signature instanceof ObjectSignature) {
				if (target != null) {
					return target;
				} else if (Provider.class.isAssignableFrom(providerType)) {
					return providerType.newInstance();
				} else {
					return instance((ObjectSignature) signature);
				}
			} else if (signature instanceof EvaluationSignature) {
				return ((EvaluationSignature) signature).getEvaluator();
			}

            // TODO VFE related
			/* else if (signature instanceof FilterSignature) {
				return ((FilterSignature) signature).getFilter();
			} else if (signature instanceof VarSignature) {
				return ((VarSignature) signature).getVar();
			}    */
		} catch (Exception e) {
			throw new SignatureException("No signature provider avaialable", e);
		}
		return provider;
	}

	/**
	 * Returns an instance by constructor method initialization or by
	 * instance/class method initialization.
	 * 
	 * @param signature
	 * @return object created
	 * @throws SignatureException
	 */
	public static Object instance(ObjectSignature signature)
			throws SignatureException {
		if (signature.getSelector() == null
				|| signature.getSelector().equals("new"))
			return ((ObjectSignature) signature).newInstance();
		else
			return ((ObjectSignature) signature).initInstance();
	}

	/**
	 * Returns an instance by class method initialization with a service
	 * context.
	 * 
	 * @param signature
	 * @return object created
	 * @throws SignatureException
	 */
	public static Object instance(ObjectSignature signature, Context context)
			throws SignatureException {
		return signature.build(context);
	}

    public static Condition condition(boolean condition) {
		return new Condition(condition);
	}

	public static OptExertion opt(String name, Exertion target) {
		return new OptExertion(name, target);
	}

	public static OptExertion opt(String name, Condition condition,
			Exertion target) {
		return new OptExertion(name, condition, target);
	}

	public static AltExertion alt(String name, OptExertion... invokers) {
		return new AltExertion(name, invokers);
	}

	public static LoopExertion loop(String name, Condition condition,
			Exertion target) {
		return new LoopExertion(name, condition, target);
	}

	public static Exertion exertion(Mappable mappable, String path)
			throws ContextException {
		Object obj = ((ServiceContext) mappable).asis(path);
        while (obj instanceof Mappable || obj instanceof Par) {
			try {
				obj = ((Evaluation) obj).asis();
			} catch (RemoteException e) {
				throw new ContextException(e);
			}
		}
		if (obj instanceof Exertion)
			return (Exertion) obj;
		else
			throw new NoneException("No such exertion at: " + path + " in: "
					+ mappable.getName());
	}
	
	public static Signature dispatcher(Signature signature) {
		((ServiceSignature)signature).addRank(Kind.DISPATCHER);
		return signature;
	}
	
	public static Signature model(Signature signature) {
		((ServiceSignature)signature).addRank(Kind.MODEL, Kind.TASKER);
		return signature;
	}
	
	public static Signature modelManager(Signature signature) {
		((ServiceSignature)signature).addRank(Kind.MODEL, Kind.MODEL_MANAGER);
		return signature;
	}
	
	public static Signature optimizer(Signature signature) {
		((ServiceSignature)signature).addRank(Kind.OPTIMIZER, Kind.TASKER);
		return signature;
	}
	
	public static Signature explorer(Signature signature) {
		((ServiceSignature)signature).addRank(Kind.EXPLORER, Kind.TASKER);
		return signature;
	}
	
	public static Block block(Signature signature, Context context,
			Exertion... exertions) throws ExertionException {
		Block block = new Block();
		block.setContext(context);
		if (signature.getClass().getName().equals(NetSignature.class.getName())) {
			try {
				block.persistContext();
			} catch (Exception e) {
				throw new ExertionException(e);
			}
		}
		block.setExertions(exertions);
		return block;
	}
}