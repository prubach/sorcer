/*
 *
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.vfe;

import static sorcer.vo.operator.context;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.id.UuidFactory;
import sorcer.co.tuple.Tuple2;
import sorcer.co.tuple.Tuple3;
import sorcer.core.SorcerConstants;
import sorcer.core.context.ApplicationDescription;
import sorcer.core.context.NullDescription;
import sorcer.core.context.model.explore.ResponseContext;
import sorcer.core.context.model.explore.Update;
import sorcer.core.context.model.par.Par;
import sorcer.core.context.model.var.FidelityInfo;
import sorcer.core.context.model.var.Realization;
import sorcer.service.Arg;
import sorcer.service.Configurable;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Describable;
import sorcer.service.Evaluation;
import sorcer.service.EvaluationException;
import sorcer.service.Invocation;
import sorcer.service.InvocationException;
import sorcer.service.Mappable;
import sorcer.service.Revaluation;
import sorcer.service.Setter;
import sorcer.service.Updatable;
import sorcer.util.GenericUtil;
import sorcer.util.Sorcer;
import sorcer.vfe.evaluator.Differentiator;
import sorcer.vfe.evaluator.ExpressionEvaluator;
import sorcer.vfe.evaluator.IndependentEvaluator;
import sorcer.vfe.evaluator.InvariantEvaluator;
import sorcer.vfe.evaluator.NullEvaluator;
import sorcer.vfe.evaluator.ProxyVarEvaluator;
import sorcer.vfe.filter.AllowableFilter;
import sorcer.vfe.filter.FileFilter;
import sorcer.vfe.filter.PatternFilter;
import sorcer.vfe.filter.PatternFilter.Pattern;
import sorcer.vfe.persist.MultiPersister;
import sorcer.vfe.randomness.Distribution;
import sorcer.vfe.util.Table;
import sorcer.vfe.util.TableList;
import sorcer.vfe.util.VarInfoList;
import sorcer.vfe.util.VarList;
import sorcer.vfe.util.VarSet;

/**
 * A generic class implementing Variability. An instance of Var holds the
 * evaluator that produces a raw value. The raw value then can be postprocessed
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Var<V> extends VarInfo<V> implements
		Variability<V>, Evaluation<V>, Invocation<V>, Mappable, Revaluation, Bonding, Comparable<V>, Configurable, Serializable,
		Describable, SorcerConstants {

	public static String VAR_COMPONENT = "sorcer.vfe.configuration.file";

	private static String CONFIG_FILE = "var.config";

	private static int tabPrintLevel = 0;
	
	// used by constants
	private V constant;
	
	final static protected Logger logger = Logger
			.getLogger(Var.class.getName());

	static final long serialVersionUID = 5750176898792335683L;

	public static Var<Double> getDerivativeVar(ServiceEvaluator<?> eval) {
		Var<Double> dvar = new Var<Double>(eval.getName() + "-pde", null,
				Type.DERIVATIVE);
		dvar.mathTypes.add(MathType.REAL);
		dvar.mathTypes.add(MathType.CONTINUOUS);
		dvar.id = UuidFactory.generate();
		dvar.evaluator = eval;
		return dvar;
	}

	// inner left-hand and right-hand side variable names specify unknown inner
	// variable yet
	// that are specified by this var realization used in objective and
	// constraint vars
	private String innerName;

	private V lastOut = null;

	// a map of linked Vars to this one
	private Map<String, Var<?>> linkedVars;

	private String rhInnerName;

	protected VarSet argVars;
	
	protected String configFilename;

	// derivative evaluators belonging to various differentiations
	protected Map<String, Differentiator> differentiators;

	// selected evaluator
	protected ServiceEvaluator evaluator;

	// evaluators belonging to various evaluations
	protected List<ServiceEvaluator> evaluators = new ArrayList<ServiceEvaluator>();

	// selected filter
	protected Filter filter;

	// filters belonging to various evaluations
	protected List<Filter> filters = new ArrayList<Filter>();

	protected Var<?> innerRhVar;

	// for delegation
	protected Var<? extends Object> innerVar;
	
	// for composition
	protected VarSet innerVars;

	protected Setter persister;

	// a name of dependent variable for the selectedGradientName
	String selectedWrt;

	public Var() {
		this("unknown" + count++);
		mathTypes.add(MathType.REAL);
		mathTypes.add(MathType.CONTINUOUS);
		id = UuidFactory.generate();
	}

	public static <V>  Var<V> newConstant(String name) {
		Var<V> v = new Var<V>();
		v.name = name == null ? ("constant" + count++) : name;
		v.constant = null;
		v.addKind(Type.CONSTANT);
		return v;
	}
	
	public void clearFilter() {
		filter = null;
		
		// removing the filter means the var state is fully
		// determined by the evaluator state, so do not
		// signal a filter change here..leave false.
		filterHasChanged = false;
	}
	
	
	public static <V>  Var<V> newConstant(String name, V constant) {
		Var<V> v = new Var<V>();
		v.name = name == null ? ("constant" + count++) : name;
		v.constant = constant;
		v.addKind(Type.CONSTANT);
		return v;
	}
	public static <V>  Var<V> newInvarinat(String name, V constant) {
		Var<V> v = new Var<V>();
		v.name = name == null ? ("invariant" + count++) : name;
		v.constant = constant;
		v.addKind(Type.INVARIANT);
		v.setEvaluator(new InvariantEvaluator(constant));
		return v;
	}
	
	public Var(String name) {
		this.name = (name == null ? ("unknown" + count++) : name);
		this.configFilename = System.getProperty(VAR_COMPONENT);
		id = UuidFactory.generate();
		// this.isFundamental = false;
		this.description = new NullDescription();
		// add default evaluator name
		realization = new Realization(this);
		realization.addEvaluatorName(name + "e");
		evaluator = new IndependentEvaluator(null);
		evaluator.setName(name + "e");
	}

	public Var(String name, Distribution distribution) {
		this(name);
		this.type = Type.INPUT;
		this.kind.add(Type.RANDOM);
		this.distribution = distribution;
	}

	public Var(String name, Double lowerBound, Double upperBound) {
		this(name, null, lowerBound, upperBound);
	}

	public Var(String name, Double value, Double lowerBound, Double upperBound) {
		this(name);
		this.type = Type.INPUT;
		this.kind.add(Type.DESIGN);
		this.kind.add(Type.BOUNDED);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		setValueType(value);
		this.evaluator = new IndependentEvaluator<Double>(value);
		this.evaluator.setName(name + "e");
	}

	public Var(String name, ServiceEvaluator evaluator) {
		this(name);
		setEvaluator(evaluator);
	}

	public Var(String name, Filter... filters) {
		this(name);
		this.type = Type.INPUT;
		evaluator = new NullEvaluator();
		this.filter = filters[0];
		for (Filter f : filters)
			this.filters.add(f);
		filterHasChanged = true;
	}

	public Var(String name, Filter filter, ServiceEvaluator evaluator)
			throws EvaluationException {
		this(name);
		this.filter = filter;
		setEvaluator(evaluator);
	}

	public Var(String name, Filter filter, ServiceEvaluator evaluator, Object value)
			throws VarException {
		this(name, filter, evaluator, value, null, false, new NullDescription());
	}

	public Var(String name, Filter filter, ServiceEvaluator evaluator, Object value,
			Class<?> realization, boolean isFundamental,
			ApplicationDescription description) throws VarException {
		this(name);
		this.filter = filter;
		this.evaluator = evaluator;
		assignEvaluator(value);
		// this.isFundamental = isFundamental;
		this.description = description;
		// ((Evaluator) evaluator).setInitialized(true);

		if (realization != null) {
			this.valueType = realization;
		}

	}

	public Var(String name, List<Filter> pipeline) throws FilterException {
		this(name);
		this.type = Type.INPUT;
		evaluator = new NullEvaluator();
		setFilterPipeline(pipeline);
	}

	public Var(String name, String innerName) {
		this(name);
		type = Type.OUTPUT;
		kind.add(Type.WATCHABLE);
		this.innerName = innerName;
	}

	public Var(String name, String innerName, Relation relation, Object alowable) {
		this(name);
		type = Type.OUTPUT;
		kind.add(Type.CONSTRAINT);
		this.innerName = innerName;
		constraintRelation = relation;
		if (alowable instanceof String)
			rhInnerName = (String) alowable;
		else
			this.allowable = alowable;
	}

	public Var(String name, String response, Target optiTarget) {
		this(name);
		type = Type.OUTPUT;
		kind.add(Type.OBJECTIVE);
		innerName = response;
		target = optiTarget;
	}

	public Var(String name, Type kind) {
		this(name, null, kind);
	}

	public Var(String name, V value, Distribution distribution) {
		this(name);
		this.type = Type.INPUT;
		this.kind.add(Type.RANDOM);
		setValueType(value);
		this.evaluator = new IndependentEvaluator<V>(value);
		this.distribution = distribution;
	}

	public Var(String name, V value, Type... types) {
		this(name);
		// default var type
		type = Type.INPUT;
		// no value set
		if (value instanceof Type) {
			if (value == Type.INPUT || value == Type.OUTPUT) {
				type = (Type) value;
				kind.addAll(Arrays.asList(types));
			} else {
				kind.add((Type) value);
				assignTyping(types);
			}
			this.evaluator = new IndependentEvaluator<V>(null);
			// value or bounded value: triplet
		} else {
			if (value instanceof Bounds) {
				Bounds bounds = (Bounds) value;
				lowerBound = bounds.getLowerBound();
				upperBound = bounds.getUpperBound();
				Double bv = bounds.getValue();
				setValueType(bv);
				addKind(Type.BOUNDED);
				this.evaluator = new IndependentEvaluator<Double>(bv);
			} else if (value instanceof Filter) {
				setFilter((Filter) value);
			} else if (value instanceof ServiceEvaluator) {
				setEvaluator((ServiceEvaluator) value);
			} else {
				setValueType(value);
				evaluator = new IndependentEvaluator<V>(value);
			}
			assignTyping(types);
		}
		evaluator.setName(name + "e");
	}

	public Var(String name, Var<?>... vars) {
		this(name, null, vars);
	}

	/**
	 * The var composition managed by the evaluator as a compositional
	 * controller. The controller is dedicated to use component vars internal
	 * structure, e.g. fidelities, while standard evaluators just uses values of
	 * their argument vars.
	 * 
	 * @param name
	 *            compositional var name
	 * @param vars
	 *            component vars
	 */
	public Var(String name, ServiceEvaluator evaluator, Var<?>... vars) {
		this(name);
		// compositional singleton-delegation
		innerVars = asInnerVarSet(vars);
		if (vars.length == 1) {
			innerVar = internInnerVar(vars[0]);
			addKind(Type.DELEGATION);
			this.evaluator.addArg(innerVar);
		}
		if (evaluator !=  null) {
			this.evaluator = evaluator;
			this.evaluator.addArgs(innerVars);
		}
		type = Type.OUTPUT;
		addKind(Type.COMPOSITION);	
		addKind(Type.LINKED);
		for (Var<?> v : innerVars) {
			if (v.linkedVars == null) {
				v.linkedVars = new HashMap<String, Var<?>>();
			}
			v.linkedVars.put(name, this);
		}
	}

	public Var(String name, Var<?> innerVar, Relation relation, Object alowable) {
		this(name);
		type = Type.OUTPUT;
		kind.add(Type.CONSTRAINT);
		this.innerVar = innerVar;
		constraintRelation = relation;
		if (alowable instanceof Var<?>)
			innerRhVar = (Var<?>) alowable;
		else
			this.allowable = alowable;
	}

	public Var(String name, Var<?> lhVar, Relation relation, Var<?> rhVar) {
		this(name);
		type = Type.OUTPUT;
		kind.add(Type.CONSTRAINT);
		innerVar = lhVar;
		constraintRelation = relation;
		innerRhVar = rhVar;
	}

	public Var(String name, VarList varList) {
		this(name);
		evaluator = new NullEvaluator();
		argVars = asArgVarSet(varList);
		type = Type.OUTPUT;
		addKind(Type.PRODUCT);
	}

	public Par getPar() {
		return new Par(name, this);
	}
	
	public Par getPar(String arg) throws VarException {
		Var var = getArgs().getVar(arg);
		return new Par(var.getName(), var);
	}
	
	public Var(String[] args) throws VarException {
		String[] configArgs;
		if (args.length == 1)
			configArgs = new String[] { CONFIG_FILE };
		configArgs = new String[] { args[1] };
		init(configArgs);
	}

	public Variability<?> addArg(Var<?> var) throws VarException {
		if (evaluator == null)
			new VarException("No evaluator for variable: " + name);
		evaluator.addArg(var);
		type = Type.OUTPUT;
		return this;
	}

	public Variability<?> addArgs(Object... vars) throws VarException {
		for (Object v : vars) {
			addArg((Var)v);
		}
		type = Type.OUTPUT;
		return this;
	}
	
	public Variability<?> addArgs(Var<?>... vars) throws VarException {
		for (Var<?> v : vars) {
			addArg(v);
		}
		type = Type.OUTPUT;
		return this;
	}

	public Variability<?> addArgs(VarList vars) throws VarException {
		for (Var<?> v : vars) {
			addArg(v);
		}
		type = Type.OUTPUT;
		return this;
	}

	/**
	 * <p>
	 * Assigns arguments of this var.
	 * </p>
	 * 
	 * @param argVars
	 *            the vars to add
	 */
	public void addArgVars(VarList vars) {
		this.argVars.addAll(vars);
	}

	public void addDifferentiator(ServiceEvaluator evaluator,
			Differentiator derivativeEvaluator) {
		if (differentiators == null)
			// new HashMap<String, Differentiator>(); //rmk was
			differentiators = new HashMap<String, Differentiator>(); // rmk now
		differentiators.put(evaluator.getName(), derivativeEvaluator);
	}

	public void addDifferentiator(String evaluatorName,
			Differentiator derivativeEvaluator) {
		if (differentiators == null)
			new HashMap<String, Differentiator>();
		differentiators.put(evaluatorName, derivativeEvaluator);
	}

	public Var<V> addEvaluator(ServiceEvaluator evaluator) {
		evaluators.add(evaluator);
		return this;
	}

	public Var<V> addEvaluator(ServiceEvaluator evaluator,
			Differentiator derivativeEvaluator) {
		evaluators.add(evaluator);
		if (differentiators == null)
			new HashMap<String, Differentiator>();
		differentiators.put(evaluator.getName(), derivativeEvaluator);
		return this;
	}

	public Var<V> addEvaluator(int index, ServiceEvaluator evaluator) {
		evaluators.add(index, evaluator);
		return this;
	}

	public void addFidelity(ServiceEvaluator evaluator, Filter filter)
			throws VarException {
		evaluators.add(evaluator);
		filters.add(filter);
	}

	public Var<V> addFilter(Filter filter) {
		filters.add(filter);
		return this;
	}

	/**
	 * Uses known values in this class constructors as well known evaluators
	 * 
	 * @param object
	 *            expected evaluator
	 */
	public void assignEvaluator(Object object) {
		if (object instanceof ServiceEvaluator)
			evaluator = (ServiceEvaluator) object;
		else
			evaluator = new IndependentEvaluator(object);

		((ServiceEvaluator) evaluator).setName(name);
	}

	public void assignTyping(Type... types) {
		super.assignTyping(types);
		for (int i = 0; i < types.length; i++) {
			if (types[i] == Type.FILTER) {
				evaluator = new NullEvaluator();
			}
		}
	}

	public void cleanSimulation() {
		evaluator.cleanSimulation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(V o) {
		if (o == null)
			throw new NullPointerException();
		if (o instanceof Var<?>)
			return name.compareTo(((Var<?>) o).getName());
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.service.Configurable#configure(java.lang.Object[])
	 */
	@Override
	public boolean configure(Object... configs)
			throws sorcer.service.ConfigurationException, RemoteException {
		Differentiator d = null;
		ServiceEvaluator e = null;
		Filter f = null;
		if (configs.length > 0) {
			for (Object u : configs) {
				if (u instanceof Differentiator)
					d = (Differentiator) u;
				else if (u instanceof Filter)
					f = (Filter) u;
				else if (u instanceof ServiceEvaluator)
					e = (ServiceEvaluator) u;
			}
			if (d != null) {
				addDifferentiator(evaluator, d);
			}
			if (e != null) {
				this.evaluator = e;
			}
			if (f != null) {
				this.filter = f;
				filterHasChanged = true;
			}
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.vfe.Fidelity#configureEvaluation(sorcer.core.context.model.explore
	 * .Update[])
	 */
	@Override
	public boolean configureFidelity(Update... varUpdates)
			throws EvaluationException, RemoteException {
		if (varUpdates.length != 1)
			throw new EvaluationException("Var requires one Update only");
		Update update = varUpdates[0];
		if (update.getVarName().equals(name)) {
			Object[] updates = update.getUpdates();
			if (updates.length > 0) {
				selectFidelity(update);
				try {
					return configure(updates);
				} catch (Exception ex) {
					throw new VarException(ex);
				}
			}
		}
		return false;
	}

	public String createDependencyString() {

		StringBuilder sb = new StringBuilder();
		sb.append("V:" + getName());
		tabPrintLevel++;

		for (Filter filt : filters) {
			String dashOrArrow = " F:";
			if (filt.getName().equals(filter.getName()))
				dashOrArrow = "*F:";
			sb.append("\n|");
			for (int j = 0; j < tabPrintLevel; j++) {
				sb.append("   ");
			}
			sb.append(dashOrArrow + filt.getName());
		}

		for (ServiceEvaluator<?> eval : evaluators) {
			String dashOrArrow = " E:";
			if (eval.getName().equals(evaluator.getName()))
				dashOrArrow = "*E:";
			sb.append("\n|");
			for (int j = 0; j < tabPrintLevel; j++) {
				sb.append("   ");
			}
			sb.append(dashOrArrow + eval.getName());
			tabPrintLevel++;
			for (int j = 0; j < tabPrintLevel; j++) {
				sb.append("   ");
			}
			sb.append(createDependencyStringEvals(eval));
			tabPrintLevel--;
		}
		tabPrintLevel--;
		return sb.toString();
	}

	public Variability<?> deleteArg(Var<?> var) throws VarException {
		if (evaluator == null)
			new VarException("No evaluator for variable: " + name);
		evaluator.deleteArg(var);
		return this;
	}

	public boolean dependsOn(Var<?> wrt, VarList chain) throws VarException {
		if (wrt == null)
			return false;
		else if (name.equals(wrt.getName()) && isIndependent()) {
			chain.add(this);
			return true;
		}
		chain.add(this);
		VarSet dependents = evaluator.args;
		for (Var<?> v : dependents) {
			if (v.dependsOn(wrt, chain)) {
				return true;
			}
		}
		chain.clear();
		return false;
	}

	public String describe() {
		StringBuilder sb = new StringBuilder();
		String g = gradientName != null ? gradientName : "";
		sb.append("[var: ")
				.append(name)
				.append("/")
				.append(type)
				.append("/")
				.append(valueType)
				.append("/")
				.append(kind)
				.append("/")
				.append(mathTypes)
				.append(" [")
				.append(evaluator != null ? "\n" + evaluator.describe() : "")
				.append(":")
				.append(filter != null ? "\n" + filter.describe() : "")
				.append(":")
				.append(getDifferentiator() != null ? "\n"
						+ getDifferentiator() : "")
				.append(":")
				.append(g)
				.append("]")
				.append("\nbounds: ")
				.append(lowerBound)
				.append('-')
				.append(upperBound)
				.append(persister != null ? "\npersister: " + persister : "")
				.append(innerVar != null ? "\ninnerVar: "
						+ innerVar.describe() : "")
				.append("\nrealization: ")
				.append(realization)
				.append("\nevaluators: " + evaluators)
				.append("\nfilters: " + filters);
		return sb.toString();
	}

	public List<Table> getAllDesignDerivativeTables() throws RemoteException,
			EvaluationException {
		List<Table> gradients = new ArrayList<Table>();
		for (ServiceEvaluator<?> ve : evaluators) {
			gradients.addAll(getDesignDerivativeTables(ve));

		}
		return gradients;
	}

	public TableList getAllPartialDerivativeTables() throws RemoteException,
			EvaluationException {
		TableList gradients = new TableList();
		for (ServiceEvaluator e : evaluators) {
			gradients.addAll(getPartialDerivativeTables(getDifferentiator(e
					.getName())));

		}
		return gradients;
	}

	public List<Table> getAllTotalDerivativeTables() throws RemoteException,
			EvaluationException {
		List<Table> gradients = new ArrayList<Table>();
		for (ServiceEvaluator e : evaluators) {
			gradients.addAll(getTotalDerivativeTables(getDifferentiator(e
					.getName())));

		}
		return gradients;
	}

	public Var<?> getArgVar(String varname) throws VarException {
		return evaluator.getArgVar(varname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.vfe.Variability#getArgs()
	 */
	@Override
	public VarSet getArgs() {
		return evaluator.args;
	}

	/**
	 * <p>
	 * Returns arguments of this var.
	 * </p>
	 * 
	 * @return the argVars
	 */
	public VarSet getArgVars() {
		return argVars;
	}

	public V asis() throws EvaluationException {
		try {
			return (V) evaluator.asis();
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	public Double getDerivative(Var<?> target, String gradientname, Var<?> wrt,
			Var<?> dependent) throws EvaluationException, RemoteException {
		Double out = null;
		Double pd = 1.0;
		Var<?> pde = null;
		VarList chain = new VarList();
		dependent.dependsOn(wrt, chain);
		if (chain.size() == 0) {
			if (wrt.equals(dependent))
				return 1.0;
			else
				return 0.0;
		} else if (chain.get(0).isKindOf(Var.Type.PARAMETER)) {
			return 0.0;
		}
		List<String> gradientVars = target.getDifferentiator().getRowNames();
		logger.fine("-----------------0 derivative: " + target.getName() + ":"
				+ name + " dependent: " + dependent.getName() + ":"
				+ wrt.getName() + ", gradient: " + gradientVars);
		if (gradientVars.contains(dependent.getName())) {
			pde = target.getDifferentiator().getDerivativeVar(gradientname,
					dependent.getName());
			Object obj = pde.getValue();
			// adjust for the result of FiniteDifferenceEvaluator
			if (obj instanceof List)
				out = (Double) ((List) obj).get(0);
			else
				out = (Double) obj;
			logger.fine("----------------1 derivative: " + target.getName()
					+ ":" + name + " dependent: " + dependent.getName() + ":"
					+ wrt.getName() + ", out: " + out);
		}

		if (chain.size() > 1 || !chain.contains(dependent)) {
			pd = getTotalDerivative(gradientName, wrt);
			out = out * pd;
			logger.fine("----------------2 derivative: " + target.getName()
					+ ":" + name + " dependent: " + dependent.getName() + ":"
					+ wrt.getName() + ", out: " + out + ":" + pd + ", chain: "
					+ chain.getNames());
		}
		return out;
	}

	public VarList getDerivativeVars(String gradientName)
			throws EvaluationException {
		return getDifferentiator().getDerivativeVars(gradientName);
	}

	public Table getDesignDerivativeTable() throws EvaluationException,
			RemoteException {
		return getDesignDerivativeTable((String) null, (String[]) null);
	}

	public Table getDesignDerivativeTable(ServiceEvaluator varEvaluator,
			String gradientName, String... wrts) throws EvaluationException,
			RemoteException {
		if (isKindOf(Type.OBJECTIVE) || isKindOf(Type.CONSTRAINT)) {
			return innerVar.getDesignDerivativeTable(gradientName, wrts);
		}
		Differentiator de = getDifferentiator(varEvaluator.getName());
		if (de != null) {
			if (wrts != null) {
				return getDesignGradient(
						name,
						gradientName == null ? getGradientName() : gradientName,
						wrts);
			} else {
				List<String> argNames = varEvaluator.args.toVarList()
						.getNames();
				String[] names = new String[argNames.size()];
				return getDesignGradient(
						name,
						gradientName == null ? getGradientName() : gradientName,
						argNames.toArray(names));
			}
		} else
			throw new EvaluationException(
					"Partial derivatives not defined by this evaluator: "
							+ name);
	}

	public Table getDesignDerivativeTable(String gradientName)
			throws EvaluationException, RemoteException {
		return getDesignDerivativeTable(gradientName, (String[]) null);
	}

	public Table getDesignDerivativeTable(String gradientName, String... wrts)
			throws EvaluationException, RemoteException {
		return getDesignDerivativeTable(evaluator, gradientName, wrts);
	}

	public Table getDesignDerivativeTable(String gradientName,
			String evaluation, String... wrts) throws EvaluationException,
			RemoteException {
		if (isKindOf(Type.OBJECTIVE) || isKindOf(Type.CONSTRAINT)) {
			return innerVar.getDesignDerivativeTable(gradientName, evaluation,
					wrts);
		}
		if (evaluation != null)
			selectFidelity(evaluation);
		Table gt = null;
		if (wrts != null && wrts.length > 0)
			gt = getDesignDerivativeTable(gradientName, wrts);
		else {
			gt = getDesignDerivativeTable(gradientName);
		}
		return gt;
	}

	public TableList getDesignDerivativeTables() throws RemoteException,
			EvaluationException {
		return getDesignDerivativeTables(evaluator);
	}

	public TableList getDesignDerivativeTables(ServiceEvaluator<?> evaluator)
			throws RemoteException, EvaluationException {
		TableList gradients = new TableList();
		Differentiator differentiator = getDifferentiator(evaluator.getName());
		List<String> gradientNames = differentiator.getColumnNames();

		for (String gn : gradientNames) {
			gradients.add(getDesignDerivativeTable(evaluator, gn,
					(String[]) null));
		}
		return gradients;
	}

	public Table getDesignGradient(String varname, String gradientName)
			throws EvaluationException, RemoteException {
		return getDesignGradient(varname, gradientName, (String[]) null);
	}

	public Table getDesignGradient(String varname, String gradientName,
			String... wrts) throws EvaluationException, RemoteException {
		List<Var<?>> wrtVars = new ArrayList<Var<?>>();
		Set<Var<?>> indepenedentVars = new HashSet<Var<?>>();
		evaluator.getIndependentVars(indepenedentVars);
		if (wrts == null)
			wrtVars.addAll(indepenedentVars);
		else {
			for (String vn : wrts) {
				for (Var<?> v : indepenedentVars) {
					if (vn.equals(v.getName())) {
						wrtVars.add(v);
					}
				}
			}
		}
		String gn = this.gradientName;
		if (gradientName != null)
			gn = gradientName;
		List<Double> out = new ArrayList<Double>(wrtVars.size());
		List<String> varNames = new ArrayList<String>(wrtVars.size());
		for (Var<?> v : wrtVars) {
			out.add(getTotalDerivative(gn, v));
			varNames.add(v.getName());
		}
		Table outTable = new Table(varNames, 2);
		outTable.setName(varname + ":" + name);
		List<String> rowNames = new ArrayList<String>();
		rowNames.add(gn);
		outTable.setRowIdentifiers(rowNames);
		outTable.addRow(out);
		return outTable;
	}

	public Differentiator getDifferentiator() {
		// logger.info("---------- DifferentiatorName: "+differentiatorName);
		// logger.info("---------- evaluatorName: "+evaluator.getName());
		if (differentiatorName != null)
			return getDifferentiator(differentiatorName);
		else
			return getDifferentiator(evaluator.getName());
	}

	public Differentiator getDifferentiator(ServiceEvaluator evaluator) {
		if (differentiators == null)
			return null;
		else
			return differentiators.get(evaluator.getName());
	}

	public Differentiator getDifferentiator(String evaluatorName) {
		if (differentiators == null)
			return null;
		else
			return differentiators.get(evaluatorName);
	}

	public Map<String, Differentiator> getDifferentiators() {
		return differentiators;
	}

	public ServiceEvaluator getEvaluator() {
		if (evaluator == null && evaluators.size() > 0)
			evaluator = evaluators.get(0);
		return evaluator;
	}

	public ServiceEvaluator getEvaluator(Differentiator de) throws VarException {
		if (differentiators == null)
			throw new VarException(
					"No evalautor for the derivative evaluator: "
							+ de.getName() + " in var: " + name);
		Set<Map.Entry<String, Differentiator>> es = differentiators.entrySet();
		for (Map.Entry<String, Differentiator> e : es) {
			if (e.getValue() == de) {
				return getEvaluator((String) e.getKey());
			}
		}
		throw new VarException("No evalautor for the derivative evaluator: "
				+ de.getName() + " in var: " + name);
	}

	public ServiceEvaluator getEvaluator(int index) throws VarException {
		ServiceEvaluator e = evaluators.get(index);
		if (e == null) {
			throw new VarException("No evalautor defined at: " + index);
		}
		return e;
	}

	public ServiceEvaluator getEvaluator(String evaluatorName) throws VarException {
		for (ServiceEvaluator eval : evaluators) {
			if (((ServiceEvaluator) eval).getName().equals(evaluatorName)) {
				return eval;
			}
		}
		throw new VarException("No evalautor defined: " + getName() + ":"
				+ evaluatorName + ":" + evaluators);
	}

	public int getEvaluatorIndex(String evaluatorName) throws VarException {
		String name = null;
		for (ServiceEvaluator e : evaluators) {
			name = e.getName();
			if (name.equals(evaluatorName)) {
				return evaluators.indexOf(e);
			}
		}
		return -1;
	}

	/**
	 * <p>
	 * Returns evaluators for various computational fidelities.
	 * </p>
	 * 
	 * @return the evaluators
	 */
	public List<ServiceEvaluator> getEvaluators() {
		return evaluators;
	}

	public VarList getFileLinkedVars() {
		VarList list = new VarList();
		Iterator i = linkedVars.values().iterator();
		Var<?> v = null;
		while (i.hasNext()) {
			v = (Var<?>) i.next();
			if (v.persister instanceof FileFilter) {
				list.add(v);
			}
		}
		return list;
	}

	public Filter getFilter() {
		return filter;
	}

	public Filter getFilter(int index) throws VarException {
		Filter f = null;
		if (filters.size() > 0) {
			f = filters.get(index);
			if (f == null) {
				throw new VarException("No evalautor defined at: " + index);
			}
		}
		return f;
	}

	public int getFilterIndex(String filterName) throws VarException {
		String name = null;
		for (Filter f : filters) {
			name = f.getName();
			logger.info("ith filter available:'" + name + "' for:'" + filterName +"'");
			if (name.equals(filterName)) {
				return filters.indexOf(f);
			}
		}
		return -1;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public List<Var> getGradientEvaluators(int index) {
		if (getDifferentiator() != null) {
			return getDifferentiator().getDataList().get(index);
		} else
			return null;
	}

	/**
	 * <p>
	 * Add purpose of method here
	 * </p>
	 * 
	 * @return the gradientName
	 */
	public String getGradientName() {
		if (gradientName == null) {
			Differentiator diff = getDifferentiator();
			if (diff == null)
				return null;
			else
				return diff.getDefaultGradientName();
		} else
			return gradientName;
	}

	public List<String> getGradientVarNames() {
		List<Var> vl = null;
		if (type.equals(Type.OUTPUT)
				&& (isKindOf(Type.CONSTRAINT) || isKindOf(Type.OBJECTIVE))) {
			vl = getGradientVars(innerVar.gradientName);
		} else
			vl = getGradientVars(gradientName);
		if (vl == null || vl.size() == 0)
			return null;
		List<String> vln = new ArrayList<String>(vl.size());
		for (Var<?> v : vl)
			vln.add(v.name);
		return vln;
	}

	public List<Var> getGradientVars(String gradientName) {
		if (type.equals(Type.OUTPUT)
				&& (isKindOf(Type.CONSTRAINT) || isKindOf(Type.OBJECTIVE))) {
			if (innerVar.getDifferentiator() != null) {
				return innerVar.getDifferentiator().getGradientVars(
						gradientName);
			}
		} else {
			if (getDifferentiator() != null) {
				return getDifferentiator().getGradientVars(gradientName);
			}
		}
		return null;
	}

	public Var<?> getIndependentVar(String varName) {
		ServiceEvaluator e = getEvaluator();
		if (name.equals(varName)) {
			if (isKindOf(Type.INPUT)
					|| (e instanceof IndependentEvaluator && !(e instanceof InvariantEvaluator)))
				return this;
			else
				return null;
		}
		return getEvaluator().getIndependentVar(varName);
	}

	/**
	 * <p>
	 * Returns the inner Variable used in the left-hand side of Constraint vars
	 * </p>
	 * 
	 * @return the innerVar
	 */
	public Var<?> getInnerLhVar() {
		return innerVar;
	}

	public String getInnerName() {
		return innerName;
	}

	/**
	 * <p>
	 * Returns the inner Variable used in the right-hand side of Constraint vars
	 * </p>
	 * 
	 * @return the innerVar
	 */
	public Var<?> getInnerRhVar() {
		return innerRhVar;
	}

	public V getInnerValue() throws EvaluationException {
		return (V) innerVar.getValue();
	}

	/**
	 * <p>
	 * Returns the inner Variable for example used in Constraint or Objective
	 * vars
	 * </p>
	 * 
	 * @return the innerVar
	 */
	public Var<?> getInnerVar() {
		return innerVar;
	}

	public String getLhName() {
		return innerName;
	}

	public Object getLhValue() throws EvaluationException {
		return innerVar.getValue();
	}

	public Map<String, Var<?>> getLinkedVars() {
		return linkedVars;
	}

	public Double getPartialDerivative(String wrtName)
			throws EvaluationException, RemoteException {
		return getDifferentiator().getPartialDerivative(gradientName, wrtName);
	}

	public Double getPartialDerivative(String gradientName, String wrtName)
			throws EvaluationException, RemoteException {
		this.gradientName = gradientName;
		return getDifferentiator().getPartialDerivative(gradientName, wrtName);
	}

	public Double getPartialDerivative(Var<?> v) throws EvaluationException,
			RemoteException {
		return getDifferentiator().getPartialDerivative(v.getName(),
				gradientName);
	}

	public double[] getPartialDerivativeGradient() throws EvaluationException,
			RemoteException {
		return getPartialDerivativeGradient(getDifferentiator(), gradientName);
	}

	public double[] getPartialDerivativeGradient(Differentiator de,
			String gradientName) throws EvaluationException, RemoteException {
		ServiceEvaluator eval = getEvaluator(de);
		List<String> varNames = de.getRowNames();
		VarList pds = eval.getProperDependents();
		if (pds.size() != varNames.size())
			throw new EvaluationException(
					"Dependent variables size does not match the corresponding size of derivative evalutor:\n"
							+ de.name + ":" + pds.getNames() + ":" + varNames);
		double[] out = new double[eval.args.size()];
		// keep the order consistent with this derivativeEvaluator
		varNames = new ArrayList<String>();
		for (int i = 0; i < pds.size(); i++) {
			out[i] = de
					.getPartialDerivative(gradientName, pds.get(i).getName());
			varNames.add(pds.get(i).getName());
		}
		return out;
	}

	public double[] getPartialDerivatives(List<String> wrtNames)
			throws EvaluationException, RemoteException {
		if (wrtNames.size() == 0)
			return getPartialDerivativeGradient();

		String[] wrtArray = new String[wrtNames.size()];
		wrtNames.toArray(wrtArray);
		return getDifferentiator()
				.getPartialDerivatives(gradientName, wrtArray);
	}

	public double[] getPartialDerivatives(String... wrtNames)
			throws EvaluationException, RemoteException {
		if (wrtNames.length == 0)
			return getPartialDerivativeGradient();
		else
			return getDifferentiator().getPartialDerivatives(gradientName,
					wrtNames);
	}

	public double[] getPartialDerivatives(Var<?>... wrtVars)
			throws EvaluationException, RemoteException {
		if (wrtVars.length == 0)
			return getPartialDerivativeGradient();

		String[] varNames = new String[wrtVars.length];
		for (int i = 0; i < wrtVars.length; i++) {
			varNames[i] = wrtVars[i].getName();
		}
		return getDifferentiator()
				.getPartialDerivatives(gradientName, varNames);
	}

	public double[] getPartialDerivatives(VarList wrtVarList)
			throws EvaluationException, RemoteException {
		if (wrtVarList.size() == 0)
			return getPartialDerivativeGradient();
		else {
			return getPartialDerivatives(wrtVarList.toArray());
		}
	}

	public Table getPartialDerivativeTable() throws EvaluationException,
			RemoteException {
		return getPartialDerivativeTable(gradientName);
	}

	public Table getPartialDerivativeTable(String gradient)
			throws EvaluationException, RemoteException {
		if (getDifferentiator() == null)
			throw new EvaluationException(
					"No derivative evaluator for variable: " + name);
		return getPartialDerivativTable(getDifferentiator(), gradient);
	}

	public TableList getPartialDerivativeTables() throws RemoteException,
			EvaluationException {
		return getPartialDerivativeTables(getDifferentiator());
	}

	public TableList getPartialDerivativeTables(Differentiator de)
			throws RemoteException, EvaluationException {
		ServiceEvaluator eval = getEvaluator(de);
		if (eval instanceof IndependentEvaluator)
			return new TableList();
		if (de == null)
			throw new EvaluationException(
					"No derivative evaluator for evaluator: " + evaluator.name);
		List<String> gradients = de.getColumnNames();
		TableList tables = new TableList(gradients.size());
		for (String gradient : gradients) {
			tables.add(getPartialDerivativTable(de, gradient));
		}
		return tables;
	}

	public Table getPartialDerivativTable(Differentiator de, String gradientName)
			throws EvaluationException, RemoteException {
		double[] outArray = getPartialDerivativeGradient(de, gradientName);
		ServiceEvaluator eval = getEvaluator(de);
		List<String> varNames = de.getRowNames();

		Table outTable = new Table(varNames, 2);
		outTable.setName(name + ":" + eval.name);
		List<String> rowNames = new ArrayList<String>();
		rowNames.add(gradientName);
		outTable.setRowIdentifiers(rowNames);
		List<Double> ld = new ArrayList<Double>(outArray.length);
		for (double d : outArray)
			ld.add(d);
		outTable.addRow(ld);
		return outTable;
	}

	public Setter getPersister() {
		return persister;
	}

	/**
	 * <p>
	 * Returns the perturbation for derivatives by by finite difference.
	 * </p>
	 * 
	 * @return the perturbation
	 */
	public double getPerturbation() {
		return evaluator.getPerturbation();
	}

	public V getPerturbedValue(String varName) throws EvaluationException {
		// first check if it is a parametric variable
		V out = null;
		if (evaluator == null && filter == null)
			throw new EvaluationException(
					"No Variable evaluator and filer defined for: " + name);
		try {
			if (evaluator == null && evaluators.size() > 0) {
				evaluator = evaluators.get(0);
				if (filters.size() > 0)
					filter = filters.get(0);
					filterHasChanged = true;
			}
			if (evaluator != null && !(evaluator instanceof NullEvaluator)) {
				out = (V) evaluator.getPerturbedValue(varName);
			}
			if (filter != null && persister == null) {
				if (filter instanceof FileFilter) {
					if (out != null) {
						evaluator = new NullEvaluator();
						filter.setValue(out);
						return out;
					}
				}
				if (filterPatternName != null
						&& (filter instanceof PatternFilter)) {
					((PatternFilter) filter).selectPattern(filterPatternName);
					out = (V) ((Filter) filter).filter(out);
				} else {
					out = (V) ((Filter) filter).filter(out);
				}
			}
			if (persister != null) {
				((PatternFilter) persister).selectPattern(persisterPatternName);
				persister.setValue(out);
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
		return out;
	}

	public Object getResponse() throws EvaluationException {
		if (innerVar != null)
			return innerVar.getValue();
		else
			throw new EvaluationException(
					"No inner variable available in var: " + name);

	}

	public Object getResponseAsIs() throws EvaluationException {
		if (innerVar != null) {
			logger.info("Var.getResponseAsIs(): calling getValueAsIs on innerVar...");
			Object out = innerVar.getValueAsIs();
			logger.info("Var.getResponseAsIs(): returning out = " + out);
			return out;
		} else {
			throw new EvaluationException(
					"No inner variable available in var: " + name);
		}
	}

	public String getRhName() {
		return rhInnerName;
	}

	public Object getRhValue() throws EvaluationException {
		return innerRhVar.getValue();
	}

	/**
	 * <p>
	 * Add purpose of method here
	 * </p>
	 * 
	 * @return the selectedWrt
	 */
	public String getSelectedWrt() {
		return selectedWrt;
	}

	public double getTotalDerivative(String gradient, Var<?> wrt)
			throws EvaluationException, RemoteException {
		Double out = 0.0;
		Double dd;
		ServiceEvaluator de;
		// logger.info("total derivative by: " + name + " wrt: " + wrt.getName()
		// + ":" + gradient + " dependents: " +
		// evaluator.dependents.getNames());
		// derivative is 0.0
		if (evaluator.args.size() == 0)
			return 0.0;

		// if (derivativeEvaluator.isFniteDifference(wrt.getName(), gradient)) {
		// de = derivativeEvaluator.getEvaluator(wrt.getName(), gradient);
		// boolean stateChanged = false;
		// if (((FiniteDifferenceEvaluator)de).isPartial()) {
		// ((FiniteDifferenceEvaluator)de).setPartial(false);
		// stateChanged = true;
		// }
		// Double dv = ((List<Double>)de.getValue()).get(0);
		// if (stateChanged)
		// ((FiniteDifferenceEvaluator)de).setPartial(false);
		// return dv;
		// }

		// for (Var<?> dependent : evaluator.dependents) {
		Var<?> dependent = null;
		VarList vlist = evaluator.args.toVarList();
		for (int i = 0; i < vlist.size(); i++) {
			// derivative is 0.0
			dependent = vlist.get(i);
			de = ((ServiceEvaluator) dependent.getEvaluator());
			if (!wrt.equals(dependent) && wrt.isIndependent()
					&& dependent.isIndependent()) {
				continue;
			}
			dd = dependent.getDerivative(this, gradient, wrt, dependent);

			logger.fine("****************** derivative by: " + name + ":"
					+ de.getName() + " dependent: " + dependent.getName() + ":"
					+ wrt.getName() + " increased by: " + dd);
			out = out + dd;
		}
		logger.fine("++++++++++++++++++ total derivative now: " + name + ":"
				+ wrt.getName() + ":" + gradient + " = " + out);
		return out;
	}

	public double getTotalDerivative(Var<?> wrt) throws EvaluationException,
			RemoteException {
		return getTotalDerivative(null, wrt);
	}

	public double[] getTotalDerivativeGradient() throws EvaluationException,
			RemoteException {
		return getTotalDerivativeGradient(getDifferentiator(), gradientName);
	}

	public double[] getTotalDerivativeGradient(Differentiator de,
			String gradient) throws EvaluationException, RemoteException {
		ServiceEvaluator eval = getEvaluator(de);
		List<String> varNames = de.getRowNames();
		VarList pds = eval.getProperDependents();
		if (pds.size() != varNames.size())
			throw new EvaluationException(
					"Dependent variables size does not match the corresponding size of derivative evalutor:\n"
							+ de.name + ":" + pds.getNames() + ":" + varNames);
		double[] out = new double[eval.args.size()];
		// keep the order consistent with this derivativeEvaluator
		varNames = new ArrayList<String>();
		Var<?> v = null;
		for (int i = 0; i < pds.size(); i++) {
			v = pds.get(i);
			out[i] = getTotalDerivative(gradient, v);
			varNames.add(v.getName());
		}
		return out;
	}

	public double[] getTotalDerivatives(List<String> wrtNames)
			throws EvaluationException, RemoteException {
		if (wrtNames.size() == 0) {
			return getTotalDerivativeGradient();
		} else {
			Var<?>[] ivs = new Var<?>[wrtNames.size()];
			Var<?> vrt;
			for (int i = 0; i < wrtNames.size(); i++) {
				vrt = getEvaluator().getArgVar(wrtNames.get(i));
				if (vrt == null)
					vrt = getIndependentVar(wrtNames.get(i));
				if (vrt == null)
					throw new EvaluationException(wrtNames.get(i)
							+ " no such wrt variable!");
				ivs[i] = vrt;
			}
			return getTotalDerivatives(gradientName, ivs);
		}
	}

	public double[] getTotalDerivatives(String... wrtNames)
			throws EvaluationException, RemoteException {
		if (wrtNames.length == 0) {
			return getTotalDerivativeGradient();
		} else {
			Var<?>[] ivs = new Var<?>[wrtNames.length];
			Var<?> vrt;
			for (int i = 0; i < wrtNames.length; i++) {
				vrt = getEvaluator().getArgVar(wrtNames[i]);
				if (vrt == null)
					vrt = getIndependentVar(wrtNames[i]);
				if (vrt == null)
					throw new EvaluationException(wrtNames[i]
							+ " no such wrt variable!");
				ivs[i] = vrt;
			}
			return getTotalDerivatives(gradientName, ivs);
		}
	}

	public double[] getTotalDerivatives(String gradientName, Var<?>... wrtVars)
			throws EvaluationException, RemoteException {
		if (wrtVars.length == 0)
			return getTotalDerivativeGradient();

		double[] tds = new double[wrtVars.length];
		for (int i = 0; i < wrtVars.length; i++) {
			tds[i] = getTotalDerivative(gradientName, wrtVars[i]);
		}
		return tds;
	}

	public double[] getTotalDerivatives(Var<?>... wrtVars)
			throws EvaluationException, RemoteException {
		return getTotalDerivatives(null, wrtVars);
	}

	public double[] getTotalDerivatives(VarList wrtVars)
			throws EvaluationException, RemoteException {
		if (wrtVars.size() == 0)
			return getTotalDerivativeGradient();

		double[] tds = new double[wrtVars.size()];
		for (int i = 0; i < wrtVars.size(); i++) {
			tds[i] = getTotalDerivative(null, wrtVars.get(i));
		}
		return tds;
	}

	public Table getTotalDerivativeTable() throws EvaluationException,
			RemoteException {
		return getTotalDerivativeTable(gradientName);
	}

	public Table getTotalDerivativeTable(Differentiator de, String gradientName)
			throws EvaluationException, RemoteException {
		double[] outArray = getTotalDerivativeGradient(de, gradientName);
		ServiceEvaluator eval = getEvaluator(de);
		List<String> varNames = de.getRowNames();

		Table outTable = new Table(varNames, 2);
		outTable.setName(name + ":" + eval.getName());
		List<String> rowNames = new ArrayList<String>();
		rowNames.add(gradientName);
		outTable.setRowIdentifiers(rowNames);
		List<Double> ld = new ArrayList<Double>(outArray.length);
		for (double d : outArray)
			ld.add(d);
		outTable.addRow(ld);
		return outTable;
	}

	public Table getTotalDerivativeTable(String gradientName)
			throws EvaluationException, RemoteException {
		String gn = this.gradientName;
		if (gradientName != null)
			gn = gradientName;
		List<String> varNames = getDifferentiator().getRowNames();
		if (evaluator.getProperDependents().size() != varNames.size())
			throw new EvaluationException(
					"Dependent variables size does not match the corresponding size of derivative evalutor:\n"
							+ getDifferentiator().getName()
							+ ":"
							+ evaluator.args.getNames() + ":" + varNames);
		List<Double> out = new ArrayList<Double>(evaluator.args.size());
		// keep the order consistent with this derivativeEvaluator
		varNames = new ArrayList<String>();
		for (Var<?> v : evaluator.getProperDependents()) {
			out.add(getTotalDerivative(gn, v));
			varNames.add(v.getName());
		}
		Table outTable = new Table(varNames, 2);
		outTable.setName(name + ":" + evaluator.name);
		List<String> rowNames = new ArrayList<String>();
		rowNames.add(gn);
		outTable.setRowIdentifiers(rowNames);
		outTable.addRow(out);
		return outTable;
	}

	public TableList getTotalDerivativeTables() throws EvaluationException,
			RemoteException {
		List<String> gradientNames = getDifferentiator().getColumnNames();
		TableList tl = new TableList(gradientNames.size());
		for (String gradientName : gradientNames)
			tl.add(getTotalDerivativeTable(gradientName));

		return tl;
	}

	public List<Table> getTotalDerivativeTables(Differentiator de)
			throws RemoteException, EvaluationException {
		ServiceEvaluator eval = getEvaluator(de);
		if (eval instanceof IndependentEvaluator)
			return new ArrayList<Table>();
		if (getDifferentiator() == null)
			throw new EvaluationException(
					"No derivative evaluator for evaluator: " + eval.name);
		List<String> gradients = de.getColumnNames();
		List<Table> tables = new ArrayList<Table>(gradients.size());
		for (String gradient : gradients) {
			tables.add(getTotalDerivativeTable(de, gradient));
		}
		return tables;
	}

	@Override
	public V getValue(Arg... entries) throws EvaluationException {	
//		 logger.info("Var.getValue(): I am Var \"" + name
//		 + "\" starting getValue(): entries = " + Arrays.toString(entries));
		if (constant != null)
			return constant;
		substitute(entries);
		V val = getValue(false);
		if (isRevaluable && val instanceof Evaluation) {
//			 logger.info("Var.getValue(): I am Var \"" + name
//					 + "\"; DOING REVALUABLE!!!");
			try {
				return (V) checkBounds(((Evaluation) val).getValue());
			} catch (Exception e) {
				String msg = "*** error: var " + name + " threw exception during "
						+ "getValue(Parameter... entries); exception = " + e + ".";
				System.out.println(msg + "; stack trace = ");
				e.printStackTrace();
				VarException ve = new VarException(msg, name, e); 
				logger.severe(msg);
				logger.throwing(this.getClass().toString(), "getValue(Parameter... entries)", ve);
				throw ve; 
			}
		} else {
			return (V)checkBounds(val);
		}
	}
	
	/* (non-Javadoc)
	 * @see sorcer.vfe.Variability#getArg(java.lang.String)
	 */
	@Override
	public V getArg(String varName) throws VarException {
		return (V) evaluator.getArg(varName);
	}

	// this is for finite-difference to get partial
	// derivatives
	public V getValue(VarInfoList argsIn) throws EvaluationException {
		for (VarInfo arg : argsIn) {
			if (!(getArgVars().getNames().contains(arg.getName()))) {
				throw new EvaluationException("***error: argument "
						+ arg.getName() + "not found in " + name);
			}
		}
		V out;
		try {
			out = (V) evaluator.getValue(argsIn);
			// filter (i.e., read glob..reduce glob to variable value)
			out = doFilter(out);
			// persist (i.e., write variable value)
			doPersist(out);
			return out;

		} catch (Exception e) {
			e.printStackTrace();
			throw new EvaluationException(e.toString());
		}
	}

	public V getValueAsIs() throws EvaluationException {
		if (constant != null)
			return constant;
		return getValue(true);
	}

	public boolean hasInnerVar() {
		if (innerVar != null)
			return true;
		return false;
	}

	public String info() {
		String f = filter != null ? filter.getName() : "";
		String e = evaluator != null ? evaluator.getName() : "";
		String esn = realization != null ? "" + realization.getEvaluatorNames()
				: null;
		String esd = esn != null ? "\nevaluators: " + esn : "";
		return "[var: " + name + "/" + type + "/" + kind + " [" + e + ":" + f
				+ "]" + esd;
	}

	public void init() throws VarException {
		if (configFilename == null)
			throw new VarException(
					"no variable configuration file available for: " + name);
		init(new String[] { configFilename });
	}

	public void init(String[] args) throws VarException {
		// get the variable configuration
		// System.out.println("init>>args: " + Arrays.toString(args));
		Configuration config;
		try {
			config = ConfigurationProvider.getInstance(args, getClass()
					.getClassLoader());
		} catch (ConfigurationException ce) {
			throw new VarException(ce);
		}
		// System.out.println("Configuration: " + config.toString());

		// variable filter
		try {
			filter = (Filter) config.getEntry("sorcer.calculus.var." + name,
					"filter", Filter.class);
			filterHasChanged = true;
		} catch (ConfigurationException e) {
			filter = null;
			filterHasChanged = true;
			e.printStackTrace();
		}
		logger.fine("config filter: " + filter);

		// variable evaluator
		try {
			evaluator = (ServiceEvaluator) config.getEntry("sorcer.calculus.var."
					+ name, "evaluator", ServiceEvaluator.class);
		} catch (ConfigurationException e) {
			evaluator = null;
			e.printStackTrace();
		}
		logger.fine("config evaluator: " + evaluator);
		if (filter == null && evaluator == null)
			throw new VarException(
					"Variable: '"
							+ name
							+ "' deployment configuration failed, filer nad evalutor not defined!");
	}

	public boolean isDependent() throws VarException {
		return !isIndependent();
	}

	public boolean isElementMathAsUsual() {
		if ((valueType.equals(Double.class))
				| (valueType.equals(Integer.class))
				| (valueType.equals(Float.class))) {
			return true;
		} else
			return false;

	}

	public boolean isIndependent() throws VarException {
		if (evaluator == null)
			throw new VarException("No ealuator for var: " + name);
		return (evaluator instanceof IndependentEvaluator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.vfe.Variability#isValueChanged()
	 */
	@Override
	public boolean isValueCurrent() {
		return evaluator.isValueCurrent();
	}

	public void persistValue(Object object) throws EvaluationException,
			RemoteException {

		if (persister != null) {
			if (persister instanceof PatternFilter) {
				try {
					((PatternFilter) persister)
							.selectPattern(persisterPatternName);
				} catch (FilterException e) {
					throw new EvaluationException(e);
				}
			}
			// logger.finest("Persisting Var described as: "+this.describe());
			logger.finest("Persiting Var named: " + this.name
					+ " with object = " + object);
			if (object instanceof ServiceEvaluator) {
				logger.finest("Instance of Evaluator, Evaluator is: " + object);
				logger.finest(">>>>>>>>>>>> Var named: " + name
						+ " Getting my value");
				Object valuessss = getValue();
				logger.finest("<<<<<<<<<<<<<< Var named: " + name
						+ " result of getValue = " + valuessss);
				logger.finest("Var named: " + name
						+ " calling persister.setValue with persister: "
						+ persister + " and value = " + valuessss);
				persister.setValue(valuessss);
				// persister.setValue(getValue());
			} else {
				persister.setValue(object);
			}

		}
	}

	public void printDependencies() {
		setDependencyString(createDependencyString());
		System.out.print(getDependencyString() + "\n");
	}

	public void removeFidelity(int index) {
		evaluators.remove(index);
		filters.remove(index);
	}

	public void resetValue(Object value) throws EvaluationException {
		if (constant != null) {
			if (isKindOf(Type.INVARIANT)) {
				// delete evaluator for allowed set operation later
				if (value == null) 
					evaluator = null;
				else {
					// or reset invariant value
					evaluator = new InvariantEvaluator(value);
					constant = (V)value;
				}
			}
		}
		setValueUpdated(value, false);
	}

	public boolean selectDifferentiation(String selectedGradientName) {
		Differentiator d = getDifferentiator();
		if (d == null)
			return false;
		else {
			List<String> names = d.getColumnNames();
			boolean hasIt = names.contains(selectedGradientName);
			if (hasIt && selectedGradientName != null)
				this.gradientName = selectedGradientName;
			else if (hasIt)
				this.gradientName = names.get(names.size() - 1);
			return hasIt;
		}
	}

	public boolean selectDifferentiation(String selectedGradientName,
			String evaluationName) throws VarException {
		boolean sd = selectDifferentiation(selectedGradientName);
		boolean se = false;
		if (sd) {
			se = selectFidelity(evaluationName);
		}
		return sd && se;
	}

	public boolean selectDifferentiation(String selectedGradientName,
			String evaluatorName, String filterName) throws VarException {
		boolean sd = selectDifferentiation(selectedGradientName);
		boolean se = false, sf = false;
		if (sd) {
			se = selectEvaluator(evaluatorName);
			sf = selectEvaluator(filterName);
		}
		return sd && se && sf;
	}

	public boolean selectDifferentiator(String derivativeEvaluatorName) {
		if (differentiators == null)
			return false;
		if (differentiators.containsKey(derivativeEvaluatorName)) {
			this.differentiatorName = derivativeEvaluatorName;
			return true;
		} else
			return false;
	}

	public boolean selectEvaluation(String evaluatorName, String filterName)
			throws VarException {
		return selectEvaluation(evaluatorName, filterName, null);
	}

	public boolean selectEvaluation(String evaluatorName, String filterName,
			String differentiatorName) throws VarException {

		boolean es = true, fs = true;
		Differentiator de = null;
		es = selectEvaluator(evaluatorName);
		fs = selectFilter(filterName);
		try {
			if (es && differentiatorName != null) {
				this.differentiatorName = differentiatorName;
				de = getDifferentiator();
			}
			if (de != null && gradientName == null) {
				gradientName = de.getColumnNames().get(
						de.getColumnNames().size() - 1);
			}
			// valueChanged();
		} catch (Exception e) {
			throw new VarException(e);
		}
		return es && fs;
	}

	public boolean selectEvaluator(String evaluatorName) throws VarException {		
		if (this.evaluator.name != null) {
			if (this.evaluator.name.equals(evaluatorName)) {
				logger.finest("Var.selectEvaluator(): returning, since "
						+ "evaluatorName is the same and already " + "selected");
				return true; 
			}
		}
		logger.finest("Var.selectEvaluator(): proceeding to try and "
				+ "select evaluatorName = " + evaluatorName);

		try {
			if (evaluatorName == null) {
				evaluator = null;
				// valueChanged();
				return true;
			}
			int eindex = getEvaluatorIndex(evaluatorName);
			if (eindex < 0)
				return false;
			ServiceEvaluator e = getEvaluator(eindex);
			if (e != null) {
				evaluator = e;
				differentiatorName = null;
				Differentiator d = getDifferentiator(e.getName());
				if (d != null) {
					gradientName = d.getColumnNames().get(
							d.getColumnNames().size() - 1);
				}
				//valueChanged();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new VarException(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.vfe.Fidelity#selectEvaluation(sorcer.core.context.model.VarEvaluation
	 * [])
	 */
	@Override
	public boolean selectFidelity(FidelityInfo... varFidelities)
			throws EvaluationException {
		if (varFidelities.length != 1)
			throw new EvaluationException("Var requires one Evaluation only");
		FidelityInfo evaluation = varFidelities[0];
		fidelityName = evaluation.getEvaluatorName();
		boolean result = false;
		logger.info("Evaluation varName = " + evaluation.getVarName()
				+ " varName = " + name);
		if (evaluation.getVarName().equals(name)) {
			logger.info("EvaluationName = " + evaluation.getName()
					+ " EvaluatorName = " + evaluation.getEvaluatorName()
					+ " FilterName = " + evaluation.getFilterName()
					+ " GradientName = " + evaluation.getGradientName());
			String evaluationName = evaluation.getName();
			String evaluatorName = evaluation.getEvaluatorName();
			String filterName = evaluation.getFilterName();
			String gradientName = evaluation.getGradientName();
			if (evaluationName != null) {
				result = selectFidelity(evaluationName);
			} else if (evaluatorName != null && filterName != null) {
				result = selectEvaluation(evaluatorName, filterName);
			} else if (evaluatorName != null) {
				result = selectFidelity(evaluatorName);
				filter = null;
				filterHasChanged = true;
			} else if (filterName != null) {
				result = selectFilter(filterName);
			}
			if (gradientName != null)
				this.gradientName = gradientName;
			else {
				if (getDifferentiator() != null)
					this.gradientName = getDifferentiator().getColumnNames()
							.get(0);
			}
		}
		return result;
	}

	public boolean selectFidelity(String evaluationName) throws VarException {
		boolean es = true, fs = true;
		Differentiator de = null;
		FidelityInfo e = realization.getEvaluation(evaluationName);
		fidelityName = evaluationName;
		if (e == null)
			return false;
		es = selectEvaluator(e.getEvaluatorName());
		fs = selectFilter(e.getFilterName());

		if (es)
			de = getDifferentiator();
		if (de != null) {
			gradientName = de.getColumnNames().get(
					de.getColumnNames().size() - 1);
		}
		return es && fs;
	}

	public boolean selectFilter(Filter filter) throws VarException {
		return selectFilter(filter.getName());
	}

	public boolean selectFilter(String filterName) throws VarException {
//		logger.info("Var.selectFilter(): I am Var \"" + getName()
//				+ "\"\n and I'm starting selectFilter...");
//
//		logger.info("Var.selectFilter(): current filterName = "
//				+ this.filterName + "\nargument filterName = " + filterName);

		if (filter != null && this.filter.name != null) {// SAB & RMK
//			logger.finest("Var.selectFilter(): current filterName = "
//					+ this.filterName + "\nargument filterName = " + filterName);
//
//			logger.finest("Var.selectFilter(): current filterName = "
//					+ this.filterName + "\nargument filterName = " + filterName);

			// if (filter != null && filter.name != null) {// SAB & RMK
			if (this.filter.name.equals(filterName)) {
//				logger.finest("Var.selectFilter(): I am Var \""
//						+ getName()
//						+ "\", and "
//						+ "\nyou are attempting to selectFilter \""
//						+ filterName
//						+ "\", which "
//						+ "\nhappens to be the current filterName...so I'm returning now. bye.");
				return true;// SAB & RMK
			}// SAB & RMK
		}// SAB & RMK
//		logger.finest("Var.selectFilter(): proceeding to try and select filterName = "
//				+ filterName);// SAB & RMK

		try {
			if (filterName == null) {
				filter = null;
				filterHasChanged = true;
				valueChanged();
				// since filter is changing, must purge cached value (lastOut)
				lastOut = null;
				return true;
			}
			int findex = getFilterIndex(filterName);
			logger.info("findex = " + findex + " For filterName: "
					+ filterName);
			if (findex < 0)
				return false;
			Filter f = getFilter(findex);
			if (f != null) {
				filter = f;
				filterHasChanged = true;
				valueChanged();
				maybeSetPatternFilterName(filter);
				// since filter is changing, must purge cached value (lastOut)
				lastOut = null;
				return true;
			}
		} catch (Exception e) {
			throw new VarException(e);
		}
		return false;
	}

	public boolean selectGradient(String gradientName) {
		if (getDifferentiator() == null)
			return false;
		List<String> columnNames = getDifferentiator().getColumnNames();
		for (String name : columnNames) {
			if (name.equals(gradientName)) {
				this.gradientName = gradientName;
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Assigns arguments of this var.
	 * </p>
	 * 
	 * @param argVars
	 *            the argVars to set
	 */
	public void setArgVars(VarList argVars) {
		this.argVars = asArgVarSet(argVars);
	}

	public void setEvaluator(ServiceEvaluator evaluator) {
		if (evaluator == null) {
			this.evaluator = new NullEvaluator();
			return;
		}
		if (isKindOf(Type.LINKED)) {
			if (!(evaluator instanceof ExpressionEvaluator))
				throw new RuntimeException(
						"Linked vars require expression evaluators only!");
			this.evaluator = evaluator;
			// removed inner var from linked Var RMK - Aug 2011
			// innerVar = new Var("linkedTo-" + name, evaluator);
			type = Type.OUTPUT;
			addKind(Type.COMPOSITION);
			// this.evaluator.addArg(innerVar);
			for (Var v : evaluator.getArgs()) {
				if (v.linkedVars == null) {
					v.linkedVars = new HashMap<String, Var<?>>();
				}
				v.linkedVars.put(name, this);
			}
			return;
		}
		this.evaluator = evaluator;
		// set a default name
		if (this.evaluator.getName() == null) {
			this.evaluator.setName(name + "e");
		}
		if (!containsEvaluator(evaluator)) {
			evaluators.add(evaluator);
		}
		if (evaluator.getArgs().size() > 0)
			type = Type.OUTPUT;
	}

	/**
	 * <p>
	 * Assigns evaluators for various computational fidelities.
	 * </p>
	 * 
	 * @param evaluators
	 *            the evaluators to set
	 */
	public void setEvaluators(List<ServiceEvaluator> evaluators) {
		this.evaluators = evaluators;
	}

	public void setFidelity(ServiceEvaluator evaluator, Filter filter)
			throws EvaluationException {
		setEvaluator(evaluator);
		differentiatorName = null;
		if (!containsEvaluator(evaluator))
			evaluators.add(evaluator);
		setFilter(filter);
		if (!containsFilter(filter))
			filters.add(filter);
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
		filterHasChanged = true;
		try {
			valueChanged();
		} catch (EvaluationException e) {
			logger.warning(e.getMessage());
		}
		if (filter != null) {
			filters.add(filter);
			// if (filter instanceof PatternFilter) {
			// if (filter instanceof FileFilter) {
			// ((FileFilter) filter).getTextFilter();
			// Pattern p = ((PatternFilter) filter).getPattern();
			// if (p != null)
			// filterPatternName = ((PatternFilter) filter)
			// .getPattern().getName();
			// }
			//
			// }
			maybeSetPatternFilterName(filter);
		}
		if (evaluator != null || evaluators.size() > 0)
			removeKind(Type.FILTER);
		else if (isKindOf(Type.FILTER))
			evaluator = new NullEvaluator();

		// below is not needed since evaluators now call
		// getValue() on their Var arguments, at which
		// time the file will be written (on demand)
		//
		// write value into a file
		// if (filter instanceof FileFilter) {
		// try {
		// this.getValue();
		// } catch (EvaluationException e) {
		// throw new RuntimeException(e);
		// }
		// }

		// need to set lastOut to null because
		// changing the filter may change the value
		// stored in this Var, so the cached value
		// (i.e., lastOut) may not be consistent
		// and the short circuits for filtering
		// and persisting compare the 'out' from
		// evaluate() with 'lastOut'...if they are
		// equivalent, then filtering and persisting
		// may be skipped. can't skip these steps
		// is the filter or persister has changed.
		lastOut = null;
	}

	public void addFidelity(Fidelity fidelity) throws FilterException {
		fidelityName = fidelity.getName();
		setEvaluator(fidelity.getEvaluator());
		setFilterPipeline(fidelity.getFilters());
		realization.addEvaluation(name, fidelity);
	}
	
	public void setFilter(Filter filter, Pattern pattern)
			throws FilterException {
		if (filter instanceof PatternFilter) {
			this.filter = filter;
			filterHasChanged = true;
			filters.add(filter);
			if (((PatternFilter) filter).getPatterns().containsValue(pattern)) {
				this.filterPatternName = pattern.getName();
			} else
				throw new FilterException(
						"The nonexistent patern in the PatternFilter: "
								+ pattern);
		} else
			throw new FilterException("The PatternFilter is required:");
	}

	public void setFilter(Filter filter, String patternName)
			throws FilterException {
		if (filter instanceof PatternFilter) {
			this.filter = filter;
			filterHasChanged = true;
			filters.add(filter);
			this.filterPatternName = ((PatternFilter) filter).getPattern(
					patternName).getName();
		} else
			throw new FilterException("The PatternFilter is required:");
	}

	@Override
	public void setFilterPatternName(String patternName) throws FilterException {
		this.filterPatternName = patternName;
		if (filter instanceof PatternFilter)
			((PatternFilter) filter).selectPattern(patternName);
		else
			throw new FilterException("No pattern filter available in: " + this);
	}

	public void setFilterPipeline(Filter[] pipeline) throws FilterException {
		if (pipeline != null && pipeline.length > 0) {
			List<Filter> filterList = Arrays.asList(pipeline);
			setFilterPipeline(filterList);
		}
	}

	public void setFilterPipeline(List<Filter> pipeline) throws FilterException {
		filter = pipeline.get(0);
		filterHasChanged = true;
		filters.add(filter);
		for (int i = 1; i < pipeline.size(); i++)
			try {
				filter.addFilter(pipeline.get(i));
			} catch (RemoteException e) {
				throw new FilterException(e);
			}
	}

	public void setFilters(Filter... filters) {
		if (filters != null && filters.length > 0) {
			this.filter = filters[0];
			filterHasChanged = true;
			for (Filter f : filters)
				this.filters.add(f);
			if (evaluator == null && evaluators.size() == 0
					&& kind.contains(Type.INPUT)) {
				evaluator = new NullEvaluator();
			}
		}
	}

	public void setFilters(List<Filter> filters) {
		Filter[] fa = new Filter[filters.size()];
		filters.toArray(fa);
		setFilters(fa);
	}

	public void setFilterTarget(String filterTarget) {
		filter.setTarget(filterTarget);
	}

	/**
	 * <p>
	 * Assigns the gradient name for this Var.
	 * </p>
	 * 
	 * @param selectedGradientName
	 *            the gradientName to set
	 */
	public void setGradientName(String selectedGradientName) {
		if (selectedGradientName != null)
			this.gradientName = selectedGradientName;
	}

	public void setInnerVar(Var var) {
		innerVar = var;
	}

	public void setLhInnerVar(Var var) {
		setInnerVar(var);
	}

	public void setPersister(Setter persister) {
//		logger.info("setPersister(): I am Var \"" + name
//				+ "\" and I'm setting persister; " + "\n\tpersister = "
//				+ persister);

		kind.add(Type.PERSISTER);
		this.persister = persister;
		evaluator.addPersister(this);
		if (persister instanceof PatternFilter) {
			persisterPatternName = new String(((PatternFilter) persister)
					.getPattern().getName());
//			logger.info("persisterPatternName:" + persisterPatternName);
		}

		// no need to do below since getValue() is called on
		// all Var args in the Evaluator evaluate() method.
		//
		// try {
		// persistValue(getValue());
		// } catch (Exception e) {
		// throw new RuntimeException(e);
		// }

		// need to set lastOut to null because
		// changing the filter may change the value
		// stored in this Var, so the cached value
		// (i.e., lastOut) may not be consistent
		// and the short circuits for filtering
		// and persisting compare the 'out' from
		// evalaute() with 'lastOut'...if they are
		// equivalent, then filtering and persisting
		// may be skipped. can't skip these steps
		// is the filter or persister has changed.
		lastOut = null;
	}
	
	public void addToMultiPersister(Setter setter) {
		Setter s = getPersister();
		if (s == null) {
			setPersister(setter);
			return;
		}
		MultiPersister mp = new MultiPersister();
		if (setter instanceof MultiPersister) {
			mp = (MultiPersister) setter;
		} else {
			mp.add(setter);
		}
		if (s instanceof MultiPersister) {
			for (Setter ss : ((MultiPersister)s)) {
				mp.add(ss);
			}
		} else {
			mp.add(s);
		}
		setPersister(mp);
	}

	@Override
	public void setPersisterPatternName(String patternName)
			throws FilterException {
		this.persisterPatternName = patternName;
		if (persister instanceof PatternFilter)
			((PatternFilter) persister).selectPattern(patternName);
		else
			throw new FilterException("No persister filter availabe in: "
					+ this);
	}

	/**
	 * <p>
	 * Assigns the perturbation for derivatives by by finite difference.
	 * </p>
	 * 
	 * @param perturbation
	 *            the perturbation to set
	 */
	public void setPerturbation(double perturbation) {
		evaluator.setPerturbation(perturbation);
	}

	public void setRhInnerVar(Var var) {
		innerRhVar = var;
	}

	/**
	 * <p>
	 * Add purpose of method here
	 * </p>
	 * 
	 * @param selectedWrt
	 *            the selectedWrt to set
	 */
	public void setSelectedWrt(String selectedWrt) {
		this.selectedWrt = selectedWrt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.vfe.Variability#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) throws EvaluationException {
		
		logger.finer("Var.setValue(): name = " + name + "; value coming in: "
				+ value);
		
		checkBounds(value);
		
		// value of constants and invariants can not be changed
		// an invariant var value can be reset or renewed
		// (if its evaluator is null)
		if (constant != null) {
			if (isKindOf(Type.INVARIANT) && evaluator == null) {
				evaluator = new InvariantEvaluator(value);
				constant = (V) value;
			}
			return;
		}
		logger.finer("setValue(): invoking setValueUpdated(); value = " + value);
		setValueUpdated(value, true);
	}

	public Object setValue(Object value, boolean isSimulated)
			throws EvaluationException, RemoteException {
		if (isSimulated) {
			return setSimulatedValue(value);
		} else {
			setValueType(value);
			setValue(value);
		}
		return this;
	}

	public void setValueLocally(V value) throws EvaluationException {
		if (filter instanceof FileFilter) {
			FileFilter ff = (FileFilter) filter;
			try {
				String dirs = Sorcer.getProperty(DOC_ROOT_DIR) + File.separator
						+ Sorcer.getProperty(P_DATA_DIR) + File.separator
						+ Sorcer.getProperty(P_SCRATCH_DIR) + File.separator
						+ getName() + "-" + ff.getName();
				File path = new File(dirs);
				if (!path.exists()) {
					boolean sucess = path.mkdirs();
					if (!sucess)
						throw new EvaluationException("Not able to craete: "
								+ dirs);
				}
				File localcopy = ((FileFilter) filter)
						.createLocalFileCopyIn(dirs);
				ff.setTarget(localcopy);
				try {
					if (filterPatternName != null) {
						ff.setValue(filterPatternName, value);
					} else {
						ff.setValue(value);
					}
					// ((Evaluator) getEvaluator()).update();
					// logger.info("Var.setValueLocally(): calling valueChanged()");
					// valueChanged();
				} catch (RemoteException e) {
					throw new EvaluationException(e);
				}
			} catch (FilterException e) {
				throw new EvaluationException(e);
			}
		} else {
			throw new EvaluationException(
					"No File Filter associated with this variable: " + name);
		}
	}

	public void setValueUpdated(Object value, boolean isUpdated)
			throws EvaluationException {
				
		// trying to fix a problem stemming from Table class
		// a set value occurs with a String object, so 'value' is
		// type String, but this Var object is already declared
		// --or should be-- a double for example
		// attempt to construct the correct 'valueType' with 'value'
		// if not..skip it
		Object newValue = value;
		if (valueType != null && value != null) {
			if (value.getClass() != valueType) {
				Class valueClass = value.getClass();
				Object val = value;
				try {
					if (value instanceof Number) {
						valueClass = String.class;
						val = ""+value;
						if (((String)val).indexOf(".") >= 0)
							valueType = Double.class;
					}
					Constructor<?> c = valueType.getConstructor(valueClass);
					newValue = c.newInstance(val);
				} catch (Exception e) {
					newValue = value;
				}
			}
		}
		setValueType(newValue);
		try {
			if (evaluator != null) {
				if (evaluator instanceof NullEvaluator
						&& (filter == null && persister == null)) {
					throw new EvaluationException(
							"No filter or persistor defined for: " + name);
				}
				evaluator.setValue(newValue);
				
				// a NullEvaluator is used when only filter is used by vars,
				// the NullEvaluator evaluator is required to propagate value changed 
				// to observers that is notified by evaluators normally 
				// but in this case this function is delegated to filter
				
				// SAB:
				// the doPersist and filter.setValue below should not be necessary, 
				// since all
				// evaluators call getValue() on their args and the doPersist
				// method is called at that time (i.e., lazy update); problems with
				// BasicFileFilter and ObjectFilter since they are used
				// to filter and persist
				// there is a strange junit example (ObjectFilterTest) that uses
				// NullEvaluator which requires persisting
				
				if (evaluator instanceof NullEvaluator) {
					if (persister != null && persister.isPersistent())
						doPersist(newValue);
					else if (filter instanceof Setter && filter.isPersistable)
						filter.setValue(newValue);
				}
			}
		} catch (Exception e) {
			throw new EvaluationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.service.Evaluation#substitute(sorcer.core.context.Path.Entry[])
	 */
	@Override
	public Var<V> substitute(Arg... entries) throws EvaluationException {
		// set the evaluator first
		if (entries == null)
			return this;
		for (Arg e : entries) {
			if (e instanceof FidelityInfo) {
				if (((FidelityInfo) e).getName() != null) {
					selectFidelity(((FidelityInfo) e).getName());
				} else {
					if (((FidelityInfo) e).getEvaluatorName() != null)
						selectEvaluator(((FidelityInfo) e).getEvaluatorName());
					if (((FidelityInfo) e).getFilterName() != null)
						selectEvaluator(((FidelityInfo) e).getFilterName());
				}
			}
		}

		VarInfoList varsInfo = new VarInfoList();
		// make substitution for the selected evaluator args
		for (Arg e : entries) {
			if (e instanceof Tuple2) {
				Object val = null;
				if (((Tuple2) e)._2 instanceof Evaluation)
					try {
						val = ((Evaluation) ((Tuple2) e)._2).getValue();
					} catch (RemoteException re) {
						throw new EvaluationException(re);
					}
				else
					val = ((Tuple2) e)._2;

				if (evaluator instanceof ProxyVarEvaluator) {
					// substitute dependent vars in in the var's remote model
					if (e instanceof Tuple2) {
						VarInfo vi = new VarInfo((String) ((Tuple2) e)._1, val);
						varsInfo.add(vi);
					} else if (e instanceof Tuple3) {
						// TODO
						// handle remote var fidelities
					}
				} else {
					if (scope != null) {
						Var v = (Var) scope.get(((Tuple2) e)._1);
						if (v != null) {
							if (e instanceof Tuple3) {
								v.selectFidelity((FidelityInfo) ((Tuple3) e)._3);
							}
							v.setValue(val);
						} else {
							throw new EvaluationException("No such var: "
									+ ((Tuple2) e)._1);
						}
					} else
						for (Var<?> v : evaluator.args) {
							if (((Tuple2) e)._1.equals(v.name)) {
								if (e instanceof Tuple3) {
									v.selectFidelity((FidelityInfo) ((Tuple3) e)._3);
								}
								v.setValue(val);
							}
						}
				}
			}
		}
		if (varsInfo.size() > 0 && evaluator instanceof ProxyVarEvaluator) {
			ResponseContext modelContext = new ResponseContext();
			try {
				((ProxyVarEvaluator) evaluator).setModelContext(context(
						modelContext, Vars.INPUTS, varsInfo));
			} catch (Exception e) {
				throw new EvaluationException(e);
			}
			((ProxyVarEvaluator) evaluator).setVarsType(Vars.OUTPUTS);
		}
		return this;
	}

	@Override
	public String toString() {
		String f = filter != null ? filter.getName() : "";
		String p = persister != null ? "" + persister : "";
		String e = evaluator != null ? evaluator.getName() : "";
		String g = gradientName != null ? gradientName : "";
		String dn = getDifferentiator() != null ? "; "
				+ getDifferentiator().getName() : "";
		return "[var: " + name + "/" + valueType + "/" + type + "/" + kind + "/" 
				+ mathTypes + " [" + e + ":" + f + ":" + dn + ":"
				+ g + "]" + "\n\tbounds: " + lowerBound + "-" + upperBound
				+ "\n\tevaluators: " + evaluators + "\n\tfilters: " + filters +
				"\n\tpersister: " + p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.vfe.Fidelity#updateEvaluation(sorcer.core.context.model.explore
	 * .Update[])
	 */
	@Override
	public boolean updateFidelity(Update... varUpdates)
			throws EvaluationException, RemoteException {
		if (varUpdates.length != 1)
			throw new EvaluationException("Var requires one Update only");
		Update update = varUpdates[0];
		if (update.getVarName().equals(name)) {
			String derivativeVarName = update.getDerivativeVarName();

			if (derivativeVarName != null
					&& derivativeVarName.equals(update.getVarName()))
				return false;
			if (derivativeVarName != null) {
				update.setVarName(derivativeVarName);
				if (isKindOf(Type.OBJECTIVE) || isKindOf(Type.CONSTRAINT))
					return innerVar.updateFidelity(update);
				else
					return updateFidelity(update);
			}
			Object[] updates = update.getUpdates();
			if (updates.length > 0) {
				ServiceEvaluator le;
				if (isKindOf(Type.OBJECTIVE) || isKindOf(Type.CONSTRAINT)) {
					innerVar.selectFidelity(update);
					le = innerVar.getEvaluator();
				} else {
					this.selectFidelity(update);
					le = evaluator;
				}

				if (le instanceof Updatable) {
					try {
						le.setChanged();
						return ((Updatable) le).update(updates);
					} catch (Exception e) {
						logger.throwing(getClass().getName(),
								"updateEvaluation", e);
						throw new VarException(e);
					}
				}
			}
		}

		// try to update derivative evaluators
		else {
			VarList derivativeVars;
			if (isKindOf(Type.OBJECTIVE) || isKindOf(Type.CONSTRAINT))
				derivativeVars = innerVar.getDifferentiator()
						.getDerivativeVars();
			else
				derivativeVars = getDifferentiator().getDerivativeVars();
			for (Var<?> v : derivativeVars) {
				if (v.name.equals(update.getVarName())) {
					ServiceEvaluator eg = v.getEvaluator();
					if (eg instanceof Updatable) {
						evaluator.setChanged();
						boolean ee = ((Updatable) eg).update(update
								.getUpdates());
						return ee;
					}
				}
			}
		}
		return false;
	}

	public void useFilterOnly(Filter filter) {
		this.filter = filter;
		filterHasChanged = true;
		filters.add(filter);
		evaluator = new NullEvaluator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.vfe.Variability#setValueChanged()
	 */
	@Override
	public void valueChanged() throws EvaluationException {
		// this is used when changing filters;
		// must notify observers of the current evaluator
		// (evaluator is still current, var value is not..filter
		// must run, so observing evaluators need to know)
		if (evaluator != null) {
			evaluator.valueChangedWithoutSettingValueIsCurrent();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.vfe.Variability#valueChanged(java.lang.Object)
	 */
	@Override
	public void valueChanged(Object obj) throws EvaluationException,
			RemoteException {
		// valueChanged();

	}

	public String getEvaluatorState() {
		if (evaluator == null) return "? ";
		return evaluator.getState();
	}
	
	public Exception getEvaluatorLastException() {
		if (evaluator == null) return null;
		return evaluator.getLastException();
	}
	
	private Object checkBounds(Object value) throws VarException {
		// this method only checks bounds and then throws exception
		// if value is outside bounds; return value is legacy..should
		// be void
		
		// null is a valid value
		if (value == null) return value;
		
		if (ignoreBounds)
			return value;
		
		//logger.info("checking bounds...");
		if (isKindOf(Type.BOUNDED) && value instanceof Double) {
			if (lowerBound != null) {
				int clb = Double.compare((Double) value, lowerBound);
				if (clb < 0) {
					//newValue = lowerBound;
					logger.warning("Var value: " + value
							+ " attempted below its lower bound: " + lowerBound);
					System.out.println("***warning: attempted var outside of bounds!");
					throw new VarException("The Var \"" + name + "\" attempted "
							+ "to set its value below its lower bound; "
							+ "\nattempted value = " + value + "\nlower bound = " + lowerBound);
				}
			}
			if (upperBound != null) {
				int glb = 0;
				glb = Double.compare((Double) value, upperBound);
				if (glb > 0) {
					//newValue = upperBound;
					logger.warning("Var value: " + value
							+ " attempted above its upper bound: " + upperBound);
					System.out.println("***warning: attempted var outside of bounds!");
					throw new VarException("The Var \"" + name + "\" attempted "
							+ "to set its value above its upper bound; "
							+ "\nattempted = " + value + "\nupper bound = " + upperBound);
				}
			}
		}
		
		return value;
	}

	private boolean containsEvaluator(ServiceEvaluator eval) {
		for (ServiceEvaluator e : evaluators) {
			if (e.getName().equals(eval.getName())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsFilter(FilterManagement filter) {
		for (FilterManagement f : filters) {
			if (f.getName().equals(filter.getName())) {
				return true;
			}
		}
		return false;
	}

	private String createDependencyStringEvals(ServiceEvaluator<?> eval) {
		Var<?>[] va = eval.getArgs().toArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < va.length; i++) {
			sb.append("\n|");
			for (int j = 0; j < tabPrintLevel; j++) {
				sb.append("   ");
			}
			sb.append(va[i].createDependencyString());
		}
		return sb.toString();
	}

	private V doFilter(Object out) throws FilterException, RemoteException {
		
		Object originalOut = out;
		
		// switch to indicate filter has run and is the current filter
		filterHasChanged = false;
		
		if (filter == null) {
			return (V)out;
		}

		try {
			logger.info("the var \""+ name + "\" calling its filter...out = " + out);
			out = (V) ((Filter) filter).filter(out);
			logger.info("the var \""+ name + "\" DONE calling its filter; out = " + out);

		} catch (Exception e) {
			
			if (originalOut != null) {
				logger.severe("***error: the var \"" + name 
						+ "\" had an exception in doFilter(); originalOut = " 
						+ originalOut + "; \n" + e.toString());
				e.printStackTrace();
				lastDoFilterWasGood = false;
				throw new FilterException(e);
			}
			// if filter throws exception, return null because
			// evaluator may have NEVER been evaluated yet
			out = null;
		}
		return (V)out;
	}

	private void doPersist(Object out) throws EvaluationException,
			RemoteException {
		if (persister == null) {
//			logger.info("Var.doPersist: I am Var \"" + name
//					+ "\", and I have NO persistors, skipping doPersist()");
			return;
		}
		
		// out == null needs to be persisted since null is a valid value for 
		// vars
		//if (persister != null && persister.isPersistable() && out != null) {
		if (persister != null && persister.isPersistent()) {
			logger.info("Var.doPersist: I am Var \"" + name
					+ "\", and I'm persisting " + "\nout = " + out);
			persister.setValue(out);
			logger.info("Var.doPersist): I am Var \"" + name
					+ "\", and I'm DONE persisting");
			return;
		}

	}

	private URL getTempURL(Filter vFilter) throws EvaluationException {
		File tmpFile = null;
		File tmpDir = Sorcer.getNewScratchDir();
		try {
			tmpFile = ((FileFilter) vFilter).createLocalFileCopyIn("" + tmpDir);
		} catch (FilterException e) {
			throw new EvaluationException(e);
		}
		try {
			return Sorcer.getScratchURL(tmpFile);
		} catch (MalformedURLException e) {
			throw new EvaluationException(e);
		}
	}

	protected V getValue(boolean getValueAsIs) throws VarException {
		V out = null;
		
		try {
			if (innerVar != null) {
				if (getValueAsIs) {
					return (V) processDelegationAsIs();
				} else {
					return (V) processDelegation();
				}
			}
			if (evaluator == null)
				throw new EvaluationException("No evaluator defined for Var: "
						+ name);
			// set default evaluator and filter if evaluator == null
			if (evaluator == null && evaluators.size() > 0) {
				evaluator = evaluators.get(evaluators.size() - 1);
				if (filters.size() > 0) {
					filter = filters.get(filters.size() - 1);
					filterHasChanged = true;
				}
				
			}
			if (evaluator != null && !(evaluator instanceof NullEvaluator)) {
				if (getValueAsIs) {
					out = (V) evaluator.asis();
				} else {
					out = (V) evaluator.getValue();
				}
			}
			if (lastOut != null && lastOut.equals(out)) {
				return out;
			}
			lastDoFilterWasGood = true;
			out = doFilter(out);
			if (lastOut != null && lastOut.equals(out)) {
				return out;
			}
			if (!getValueAsIs) {
				doPersist(out);
			}
		} catch (Exception e) {
			String msg = "*** error: var " + name + " threw exception during "
					+ "getValue(); exception = " + e + "; "
					+ "stack trace = \n" + GenericUtil.arrayToString(e.getStackTrace(), false);
			e.printStackTrace();
			logger.severe(msg);
			VarException ve = new VarException(msg, name, e);
			logger.throwing(this.getClass().toString(), "getValue(boolean getValueAsIs)", ve);
			throw ve;
		}
		
		if (!getValueAsIs) lastOut = out;

		return out;
	}
	
	private boolean lastDoFilterWasGood = true;
	
	private void maybeSetPatternFilterName(Filter filter) {
		if (filter instanceof PatternFilter) {
			if (filter instanceof FileFilter) {
				((FileFilter) filter).getTextFilter();
			}
			Pattern p = ((PatternFilter) filter).getPattern();
			if (p != null)
				filterPatternName = ((PatternFilter) filter).getPattern()
						.getName();
		}
	}

	private Object processDelegation() throws EvaluationException,
			RemoteException {
		if (isKindOf(Type.CONSTRAINT)) {
			Filter f = null;
			if (innerRhVar != null) {
				f = new AllowableFilter(constraintRelation, innerRhVar);
				f.setTarget(innerVar.getValue());
			} else {
				f = new AllowableFilter(constraintRelation, allowable);
				f.setTarget(innerVar.getValue());
			}
			return f.getValue();
		}
		return getInnerValue();
	}

	private Object processDelegationAsIs() throws EvaluationException,
			RemoteException {
		if (isKindOf(Type.CONSTRAINT)) {
			Filter f = null;
			Object innverVarValueAsIs = innerVar.getValueAsIs();

			if (innerRhVar != null) {
				f = new AllowableFilter(constraintRelation, innerRhVar);
				f.setTarget(innverVarValueAsIs);
			} else {
				f = new AllowableFilter(constraintRelation, allowable);
				f.setTarget(innverVarValueAsIs);
			}
			return f.getValue();
		}
		return getResponseAsIs();
	}

	private Object setSimulatedValue(Object value) throws EvaluationException,
			RemoteException {
		if (filter instanceof Setter) {
			if (filter instanceof FileFilter) {
				Object obj = filter.getTarget();
				URL initURL = null, initLinkedURL = null;
				if (obj instanceof URL)
					initURL = (URL) obj;
				else {
					// assume it is a file
					try {
						initURL = new URL("file://" + obj);
					} catch (MalformedURLException urle) {
						throw new EvaluationException(urle);
					}
				}
				URL tempURL = getTempURL(filter);

				((FileFilter) filter).setTarget(tempURL);
				filter.setValue(value);
				VarList fileLinked = null;
				URL tempLinkedURL = null;
				// create a temp file shared by all linked vars
				if (linkedVars != null) {
					fileLinked = getFileLinkedVars();
					if (fileLinked.size() > 0) {
						Filter lvPersister = (Filter) fileLinked.get(0).persister;
						Object lvObject = lvPersister.getTarget();
						if (lvObject instanceof URL) {
							initLinkedURL = (URL) lvObject;
						} else {
							// assume it is a file
							try {
								initLinkedURL = new URL("file://" + obj);
							} catch (MalformedURLException urle) {
								throw new EvaluationException(urle);
							}
						}
						if (fileLinked.size() == 1
								&& lvPersister.getTarget().equals(lvObject)) {
							tempLinkedURL = tempURL;
							lvPersister.setTarget(tempLinkedURL);
							try {
								lvPersister.setValue(fileLinked.get(0)
										.getPersisterPatternName(), value);
							} catch (FilterException e) {
								throw new EvaluationException(e);
							}
							initLinkedURL = (URL) obj;
						} else {
							tempLinkedURL = getTempURL(lvPersister);
							for (Var<?> lv : fileLinked) {
								if (lv.persister instanceof FileFilter) {
									((FileFilter) lv.persister)
											.setTarget(tempLinkedURL);
									try {
										((Filter) lv.persister).setValue(
												lv.getPersisterPatternName(),
												value);
									} catch (FilterException e) {
										throw new EvaluationException(e);
									}
								}
							}
						}
					}
				}
				Tuple2<URL, URL> ve = new Tuple2<URL, URL>(initURL, tempURL);
				if (fileLinked != null && fileLinked.size() == 1) {
					Tuple2<URL, URL>[] ves = new Tuple2[2];
					ves[0] = ve;
					ves[1] = new Tuple2<URL, URL>(initLinkedURL, tempLinkedURL);
					return ves;
				}
				return ve;
			}
		} else {
			evaluator.setValue(value, true);
		}
		return this;
	}
	
	private VarSet asArgVarSet(VarList varList) {
		VarSet vs = new VarSet();
		for (Var<?> v : varList) {
			vs.add(internArgVar(v));
		}
		return vs;
	}
	 
	private VarSet asInnerVarSet(Var[] varList) {
		VarSet vs = new VarSet();
		for (Var<?> v : varList) {
			vs.add(internInnerVar(v));
		}
		return vs;
	}
	
	protected Var<?> internInnerVar(Var<?> var) {
		if (innerVars == null)
			innerVars = new VarSet();
		Var<?> iv = var;
		if (innerVars.contains(var)) {
			Iterator<Var> i = innerVars.iterator();
			while (i.hasNext()) {
				iv = i.next();
				if (iv.equals(var)) {
					break;
				}
			}
		} else {
			innerVars.add(var);
		}
		return iv;
	}
	
	protected Var<?> internArgVar(Var<?> var) {
		Var<?> iv = var;
		if (argVars.contains(var)) {
			Iterator<Var> i = innerVars.iterator();
			while (i.hasNext()) {
				iv = i.next();
				if (iv.equals(var)) {
					break;
				}
			}
		} else {
			innerVars.add(var);
		}
		return iv;
	}
	
	// used only to reset invariant Vars
	public V empty() {
		if (isKindOf(Type.INVARIANT)) {
			V old = constant;
			constant = null;
			return old;
		} else
			return null;
	}
	
	
	public String getVarState() {
		
		String varState = VarInfo.VAR_STATE_UNKNOWN;
		
		if (!lastDoFilterWasGood) return VarInfo.VAR_STATE_NOT_CURRENT_WITH_EXCEPTION;

		if (evaluator == null) return VarInfo.VAR_STATE_UNKNOWN;
		
		String es = evaluator.getState();
		
		if (es.equals(ServiceEvaluator.EVAL_STATE_CURRENT) 
				|| es.equals(ServiceEvaluator.EVAL_STATE_INITIAL)) {
			if (filterHasChanged) {
				return VarInfo.VAR_STATE_FILTER_NOT_CURRENT;
			} else {
				return VarInfo.VAR_STATE_CURRENT;
			}
		}
		
		if (es.equals(ServiceEvaluator.EVAL_STATE_EVALUATING)) return VarInfo.VAR_STATE_EVALUATING;
			
		if (es.equals(ServiceEvaluator.EVAL_STATE_NOT_CURRENT)) {
			if (filterHasChanged) {
				return VarInfo.VAR_STATE_EVAL_AND_FILTER_NOT_CURRENT;
			} else {
				return VarInfo.VAR_STATE_EVAL_NOT_CURRENT;
			}
		}
		
		if (es.equals(ServiceEvaluator.EVAL_STATE_NOT_CURRENT_WITH_EXCEPTION))
			return VarInfo.VAR_STATE_NOT_CURRENT_WITH_EXCEPTION;
		
		return varState;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#getValue(java.lang.String)
	 */
	@Override
	public V getValue(String path, Arg... args) throws ContextException {
		return (V)scope.getValue(path, args);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#asis(java.lang.String)
	 */
	@Override
	public Object asis(String path) throws ContextException {
		return scope.asis(path);
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Mappable#putValue(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object putValue(String path, Object value) throws ContextException {
		scope.putValue(path, value);
		return scope;
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Invocation#invoke(sorcer.service.Arg[])
	 */
	@Override
	public V invoke(Arg... entries) throws RemoteException, InvocationException {
		try {
			return getValue(entries);
		} catch (EvaluationException e) {
			throw new InvocationException(e);
		}
	}

	/* (non-Javadoc)
	 * @see sorcer.service.Invocation#invoke(sorcer.service.Context, sorcer.service.Arg[])
	 */
	@Override
	public V invoke(Context context, Arg... entries) throws RemoteException,
			InvocationException {
		try {
			scope.append(context);
			return getValue(entries);
		} catch (Exception e) {
			throw new InvocationException(e);
		}
	}

}
