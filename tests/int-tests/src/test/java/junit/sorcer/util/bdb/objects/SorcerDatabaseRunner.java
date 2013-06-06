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
package junit.sorcer.util.bdb.objects;

import static sorcer.eo.operator.context;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.job;
import static sorcer.eo.operator.out;
import static sorcer.eo.operator.path;
import static sorcer.eo.operator.pipe;
import static sorcer.eo.operator.sig;
import static sorcer.eo.operator.task;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.sorcer.core.provider.Adder;
import junit.sorcer.core.provider.Multiplier;
import junit.sorcer.core.provider.Subtractor;
import junit.sorcer.core.provider.exertmonitor.SessionDatabaseRunner;
import sorcer.core.context.ServiceContext;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.EvaluationException;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.service.Job;
import sorcer.service.SignatureException;
import sorcer.service.Task;
import sorcer.util.bdb.objects.SorcerDatabase;
import sorcer.util.bdb.objects.SorcerDatabaseViews;
import sorcer.util.bdb.objects.UuidObject;

import com.sleepycat.collections.StoredValueSet;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;
//import static sorcer.vo.operator.inputVars;
//import static sorcer.vo.operator.outputVars;
//import static sorcer.vo.operator.parametricModel;
//import sorcer.core.dataContext.model.ParametricModel;
//import sorcer.core.dataContext.model.VarModel;
//import sorcer.vfe.Var;
//import sorcer.vfe.util.Table;
/**
 * SessionDatabaseRunner is the main entry point for the program and may be run as
 * follows:
 * 
 * <pre>
 * java sorcer.core.provider.exertmonitor.db
 *      [-h <home-directory> ]
 * </pre>
 * 
 * <p>
 * The default for the home directory is ./tmp -- the tmp subdirectory of the
 * current directory where the ServiceProviderDB is run. To specify a different
 * home directory, use the -home option. The home directory must exist before
 * running the sample. To recreate the sample database from scratch, delete all
 * files in the home directory before running the sample.
 * </p>
 * 
 * @author Mike Sobolewski
 */
/**
     * Run the sample program.
     */
// Parse the command line arguments.
//
// Run the sample.
// If an exception reaches this point, the last transaction did not
// complete.  If the exception is RunRecoveryException, follow
// the Berkeley DB recovery procedures before running again.
// Always attempt to close the database cleanly.
/**
	 * Open the database and views.
	 */
/**
     * Close the database cleanly.
     */
/**
     * Run two transactions to populate and print the database.  A
     * TransactionRunner is used to ensure consistent handling of transactions,
     * including deadlock retries.  But the best transaction handling mechanism
     * to use depends on the application.
     */
//		requestor.run(new PopulateTableDatabase());
//		requestor.run(new PopulateVarDatabase());
//		requestor.run(new PopulateVarModelsDatabase());
//		requestor.run(new PrintTableDatabase());
//		requestor.run(new PrintVarDatabase());
//		requestor.run(new PrintVarModelDatabase());
/**
     * Populate the Context database in a single transaction.
     */
/**
     * Populate the Table database in a single transaction.
     */
//	private class PopulateTableDatabase implements TransactionWorker {
//
//		public void doWork() {
//			try {
//				addTables();
//			} catch (EvaluationException e) {
//				e.printStackTrace();
//			}
//		}
//	}
/**
     * Populate the Exertion database in a single transaction.
     */
/**
     * Populate the Var database in a single transaction.
     */
//	private class PopulateVarDatabase implements TransactionWorker {
//
//		public void doWork() {
//			try {
//				addVars();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
/**
     * Populate the Var database in a single transaction.
     */
/**
 * SessionDatabaseRunner is the main entry point for the program and may be run as
 * follows:
 * 
 * <pre>
 * java sorcer.core.provider.exertmonitor.db
 *      [-h <home-directory> ]
 * </pre>
 * 
 * <p>
 * The default for the home directory is ./tmp -- the tmp subdirectory of the
 * current directory where the ServiceProviderDB is run. To specify a different
 * home directory, use the -home option. The home directory must exist before
 * running the sample. To recreate the sample database from scratch, delete all
 * files in the home directory before running the sample.
 * </p>
 * 
 * @author Mike Sobolewski
 */
public class SorcerDatabaseRunner {

    private final SorcerDatabase sdb;
    
    private SorcerDatabaseViews views;

	/**
     * Run the sample program.
     */
    public static void main(String... args) {
    	if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
        System.out.println("\nRunning sample: " + SessionDatabaseRunner.class);

        // Parse the command line arguments.
        //
        String homeDir = "./tmp";
        for (int i = 0; i < args.length; i += 1) {
            if (args[i].equals("-h") && i < args.length - 1) {
                i += 1;
                homeDir = args[i];
            } else {
                System.err.println("Usage:\n java " + SessionDatabaseRunner.class.getName() +
                                   "\n  [-h <home-directory>]");
                System.exit(2);
            }
        }

        // Run the sample.
        SessionDatabaseRunner runner = null;
        try {
            runner = new SessionDatabaseRunner(homeDir);
            runner.run();
        } catch (Exception e) {
            // If an exception reaches this point, the last transaction did not
            // complete.  If the exception is RunRecoveryException, follow
            // the Berkeley DB recovery procedures before running again.
            e.printStackTrace();
        } 
        finally {
            if (runner != null) {
                try {
                    // Always attempt to close the database cleanly.
                    runner.close();
                } catch (Exception e) {
                    System.err.println("Exception during database close:");
                    e.printStackTrace();
                }
            }
        }
    }

	/**
	 * Open the database and views.
	 */
	public SorcerDatabaseRunner(String homeDir) throws DatabaseException {
		sdb = new SorcerDatabase(homeDir);
		views = new SorcerDatabaseViews(sdb);
	}

    /**
     * Close the database cleanly.
     */
    public void close()
        throws DatabaseException {

        sdb.close();
    }

    /**
     * Run two transactions to populate and print the database.  A
     * TransactionRunner is used to ensure consistent handling of transactions,
     * including deadlock retries.  But the best transaction handling mechanism
     * to use depends on the application.
     */
	public void run() throws Exception {
		TransactionRunner runner = new TransactionRunner(sdb.getEnvironment());
		runner.run(new PopulateContextDatabase());
//		requestor.run(new PopulateTableDatabase());
		runner.run(new PopulateExertionDatabase());
//		requestor.run(new PopulateVarDatabase());
//		requestor.run(new PopulateVarModelsDatabase());
		runner.run(new PopulateUuidObjectDatabase());
		
		runner.run(new PrintContextDatabase());
//		requestor.run(new PrintTableDatabase());
		runner.run(new PrintExertionDatabase());
//		requestor.run(new PrintVarDatabase());
//		requestor.run(new PrintVarModelDatabase());
		runner.run(new PrintUuidObjectDatabase());
	}

    /**
     * Populate the Context database in a single transaction.
     */
	private class PopulateContextDatabase implements TransactionWorker {

		public void doWork() {
			addContexts();
		}
	}

	 /**
     * Populate the Table database in a single transaction.
     */
//	private class PopulateTableDatabase implements TransactionWorker {
//
//		public void doWork() {
//			try {
//				addTables();
//			} catch (EvaluationException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	 /**
     * Populate the Exertion database in a single transaction.
     */
	private class PopulateExertionDatabase implements TransactionWorker {

		public void doWork() {
			try {
				addExertions();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	 /**
     * Populate the Var database in a single transaction.
     */
//	private class PopulateVarDatabase implements TransactionWorker {
//
//		public void doWork() {
//			try {
//				addVars();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	 /**
     * Populate the Var database in a single transaction.
     */
//	private class PopulateVarModelsDatabase implements TransactionWorker {
//
//		public void doWork() {
//			try {
//				addVarModels();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	/**
    * Populate the UuidObject database in a single transaction.
    */
	private class PopulateUuidObjectDatabase implements TransactionWorker {

		public void doWork() {
			try {
				addUuidObjects();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    /**
     * Print the database in a single transaction.  All entities are printed
     * and the indices are used to print the entities for certain keys.
     *
     * <p> Note the use of special iterator() methods.  These are used here
     * with indices to find the runtimes for certain providers.</p>
     */
    private class PrintContextDatabase implements TransactionWorker {

        public void doWork() {
        	try {
				printValues("Contexts", views.getContextSet().iterator());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
    }

//    private class PrintTableDatabase implements TransactionWorker {
//
//        public void doWork() {
//        	try {
//				printValues("Tables", views.getTableSet().iterator());
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//        }
//    }
    
    private class PrintExertionDatabase implements TransactionWorker {

        public void doWork() {
        	try {
				printValues("Exertions", views.getExertionSet().iterator());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
    }

//    private class PrintVarDatabase implements TransactionWorker {
//
//        public void doWork() {
//        	try {
//				printValues("Vars", views.getVarSet().iterator());
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//        }
//    }
        
//    private class PrintVarModelDatabase implements TransactionWorker {
//
//        public void doWork() {
//        	try {
//				printValues("VarModels", views.getVarModelSet().iterator());
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//        }
//    }
    
    private class PrintUuidObjectDatabase implements TransactionWorker {

        public void doWork() {
        	try {
				printValues("Object", views.getUuidObjectSet().iterator());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Populate the dataContext entities in the database.
     * @throws IOException 
     */
	private void addContexts(ArrayList<Context> contexts) throws IOException {
		Set<Context> contextSet = views.getContextSet();
        contextSet.addAll(contexts);
    }

	 /**
     * Populate the dataContext in the database.
     */
	private void addContext(Context context) {
		Set<Context> contextSet = views.getContextSet();
        contextSet.add(context);
    }
	
	 /**
     * Populate the dataContext entities in the database.
     */
	private void addContexts() {
		StoredValueSet<Context> contextSet = views.getContextSet();
        	contextSet.add(new ServiceContext("c1"));
        	contextSet.add(new ServiceContext("c2"));
        	contextSet.add(new ServiceContext("c3"));
    }
	
	 /**
     * Populate the tables entities in the database.  
	 * @throws EvaluationException 
     */
//	private void addTables() throws EvaluationException {
//		StoredValueSet<Table> tableSet = views.getTableSet();
//        	tableSet.add((new Table()));
//        	tableSet.add(new Table());
//        	tableSet.add(new Table());
//    }
	
	private Task getTask() throws ExertionException, SignatureException, ContextException {
		Task f4 = task("f4", sig("multiply", Multiplier.class), 
				context("multiply", in(path("arg/x1"), 10.0), in(path("arg/x2"), 50.0),
						out(path("result/y1"), null)));
		
		return f4;
	}
		
	private Job getJob() throws ExertionException, SignatureException, ContextException {
		Task f4 = task("f4", sig("multiply", Multiplier.class), 
				context("multiply", in(path("arg/x1"), 10.0), in(path("arg/x2"), 50.0),
						out(path("result/y1"), null)));

		Task f5 = task("f5", sig("add", Adder.class), 
				context("add", in(path("arg/x3"), 20.0), in(path("arg/x4"), 80.0),
						out(path("result/y2"), null)));

		Task f3 = task("f3", sig("subtract", Subtractor.class), 
				context("subtract", in(path("arg/x5"), null), in(path("arg/x6"), null),
						out(path("result/y3"), null)));

		// Service Composition f1(f2(x1, x2), f3(x1, x2))
		// Service Composition f2(f4(x1, x2), f5(x1, x2))
		//Job f1= job("f1", job("f2", f4, f5, strategy(Flow.PAR, Access.PULL)), f3,
		Job f1= job("f1", job("f2", f4, f5), f3,
				pipe(out(f4, path("result/y1")), in(f3, path("arg/x5"))),
				pipe(out(f5, path("result/y2")), in(f3, path("arg/x6"))));
		return f1;
	}
	
	 /**
     * Populate the exertion entities in the database.  
	 * @throws ContextException 
	 * @throws SignatureException 
	 * @throws ExertionException 
     */
	private void addExertions() throws ExertionException, SignatureException, ContextException {
		StoredValueSet<Exertion> exertionSet = views.getExertionSet();
		exertionSet.add(getTask());
		exertionSet.add(getJob());
	}

	 /**
     * Populate the Var entities in the database.  
     */
//	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
//	private void addVars() {
//		StoredValueSet<Var> varSet = views.getVarSet();
//		varSet.add(new Var("v1", 10.0));
//		varSet.add(new Var("v2", "mike"));
//	}
	
	 /**
     * Populate the VarModel entities in the database.  
	 * @throws EvaluationException 
     */
//	private void addVarModels() throws EvaluationException {
//		ParametricModel m1 = parametricModel("m1", 
//				inputVars("x", 2), 
//				outputVars("f"),
//				outputVars("g", 2));
//		
//		ParametricModel m2 = parametricModel("m2", 
//				inputVars("x", 2), 
//				outputVars("f"),
//				outputVars("g", 2));
//		
//		StoredValueSet<VarModel> varModelSet = views.getVarModelSet();
//
//		varModelSet.add(m1);
//		varModelSet.add(m2);
//	}
	
	 /**
     * Populate the VarModel entities in the database.  
	 * @throws EvaluationException 
     */
	private void addUuidObjects() throws EvaluationException {
		StoredValueSet<UuidObject> uuidObjetSet = views.getUuidObjectSet();
		uuidObjetSet.add(new UuidObject("Mike"));
		uuidObjetSet.add(new UuidObject("Sobolewski"));
	}
	
	 /**
     * Get the dataContext names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
	public List<String> returnContextNames() throws IOException, ClassNotFoundException {
		List<String> names = new ArrayList<String>();
		Iterator<Context> iterator = views.getContextSet().iterator();
		while (iterator.hasNext()) {
			Context cxt = iterator.next();
			names.add(cxt.getName());
		}
		return names;
	}
		
	
	 /**
     * Get the table names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
//	public List<String> returnTableNames() throws IOException, ClassNotFoundException {
//		List<String> names = new ArrayList<String>();
//		Iterator<Table> iterator = views.getTableSet().iterator();
//		while (iterator.hasNext()) {
//			Table table = iterator.next();
//			names.add(table.getName());
//		}
//		return names;
//	}
	
	 /**
     * Get the exertion names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
	public List<String> returnExertionNames() throws IOException, ClassNotFoundException {
		List<String> names = new ArrayList<String>();
		Iterator<Exertion> iterator = views.getExertionSet().iterator();
		while (iterator.hasNext()) {
			Exertion xrt = iterator.next();
			names.add(xrt.getName());
		}
		return names;
	}
	
	 /**
     * Get the var names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
//	public List<String> returnVarNames() throws IOException, ClassNotFoundException {
//		List<String> names = new ArrayList<String>();
//		Iterator<Var> iterator = views.getVarSet().iterator();
//		while (iterator.hasNext()) {
//			Var var = iterator.next();
//			names.add(var.getName());
//		}
//		return names;
//	}
	
	 /**
     * Get the var model names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
//	public List<String> returnVarModelNames() throws IOException, ClassNotFoundException {
//		List<String> names = new ArrayList<String>();
//		Iterator<VarModel> iterator = views.getVarModelSet().iterator();
//		while (iterator.hasNext()) {
//			VarModel varModel = iterator.next();
//			names.add(varModel.getName());
//		}
//		return names;
//	}
	
	 /**
     * Get the UuidObject names returned by an iterator of entity value objects.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
     */
	public List<String> returnUuidObjectNames() throws IOException, ClassNotFoundException {
		List<String> names = new ArrayList<String>();
		Iterator<UuidObject> iterator = views.getUuidObjectSet().iterator();
		while (iterator.hasNext()) {
			UuidObject object = iterator.next();
			names.add("" + object.getObject());
		}
		return names;
	}
	
    /**
     * Print the objects returned by an iterator of entity value objects.
     * @throws ClassNotFoundException 
     * @throws IOException 
     */
	private void printValues(String label, Iterator iterator) throws IOException, ClassNotFoundException {
		System.out.println("\n--- " + label + " ---");
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			System.out.println(obj);
		}
	}
	
	public SorcerDatabaseViews getViews() {
		return views;
	}
}
