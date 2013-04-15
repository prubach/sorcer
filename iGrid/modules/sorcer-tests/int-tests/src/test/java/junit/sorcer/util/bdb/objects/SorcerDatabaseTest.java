package junit.sorcer.util.bdb.objects;

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.list;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import sorcer.core.SorcerConstants;
//import sorcer.core.context.model.VarModel;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.util.Sorcer;
import sorcer.util.SorcerUtil;
import sorcer.util.bdb.objects.UuidKey;
import sorcer.util.bdb.objects.UuidObject;
//import sorcer.vfe.Var;
//import sorcer.vfe.util.Table;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;

/**
 * @author Mike Sobolewski
 */

public class SorcerDatabaseTest implements SorcerConstants {

	private final static Logger logger = Logger
			.getLogger(SorcerDatabaseTest.class.getName());
	
	static {
		System.setProperty("java.security.policy", System.getenv("IGRID_HOME")
				+ "/configs/policy.all");
		System.setSecurityManager(new RMISecurityManager());
		Sorcer.setCodeBaseByArtifacts(new String[] {
				"org.sorcersoft.sorcer:sorcer-api",
				"org.sorcersoft.sorcer:ju-arithmetic-api" });
	}
	
	private static SorcerDatabaseRunner runner;
	private static File dbDir;
	
	@BeforeClass 
	public static void setUpOnce() throws IOException, DatabaseException {
		dbDir = new File("./tmp/ju-sorcer-db");
		System.out.println("Sorcer DB dir: " + dbDir.getCanonicalPath());
		dbDir.mkdirs();
		String homeDir = "./tmp/ju-sorcer-db";
		runner = new SorcerDatabaseRunner(homeDir);
	}
	
	@AfterClass 
	public static void cleanup() throws Exception {
		// delete database home directory and close database
		SorcerUtil.deleteDir(dbDir);
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
	
	@Test
	public void storedcContextSetTest() throws Exception {
        runner.run();
        // get from the database three contexts persisted   
		List<String> names = runner.returnContextNames();
		List<String> ln = list("c1", "c2", "c3");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
	@Test
	public void storedContextMapTest() throws Exception {
		StoredMap<UuidKey, Context> sm = runner.getViews()
				.getContextMap();
		
		Iterator<Map.Entry<UuidKey, Context>> mei = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, Context> entry = null;

		while (mei.hasNext()) {
			entry = mei.next();
			names.add(entry.getValue().getName());
		}
		List<String> ln = list("c1", "c2", "c3");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
//	@Test
//	public void storedTableSetTest() throws Exception {
//		// the second run and the second db population
//        runner.run();
//        // get from the database three tables persisted twice
//		List<String> names = runner.returnTableNames();
//		List<String> ln = list("undefined0", "undefined1", "undefined2", "undefined3", "undefined4", "undefined5");
//		Collections.sort(names);
//		logger.info("table names: " + names);
//		
//		assertEquals(names, ln);
//	}
//	
//	@Test
//	public void storedTableMapTest() throws Exception {
//		StoredMap<UuidKey, Table> sm = runner.getViews()
//				.getTableMap();
//		
//		Iterator<Map.Entry<UuidKey, Table>> it = sm
//				.entrySet().iterator();
//				
//		List<String> names = new ArrayList<String>();
//		Map.Entry<UuidKey, Table> entry = null;
//
//		while (it.hasNext()) {
//			entry = it.next();
//			names.add(entry.getValue().getName());
//		}
//		List<String> ln = list("undefined0", "undefined1", "undefined2", "undefined3", "undefined4", "undefined5");
//		Collections.sort(names);
//		logger.info("table names: " + names);
//		
//		assertEquals(names, ln);
//	}
	
	@Test
	public void storedExertionSetTest() throws Exception {
        // get from the database two exertions persisted twice
		List<String> names = runner.returnExertionNames();
		List<String> ln = list("f1", "f4");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
	@Test
	public void storedExertionMapTest() throws Exception {
		StoredMap<UuidKey, Exertion> sm = runner.getViews()
				.getExertionMap();
		
		Iterator<Map.Entry<UuidKey, Exertion>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, Exertion> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getName());
		}
		List<String> ln = list("f1", "f4");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
//	@Test
//	public void storedVarSetTest() throws Exception {
//        // get from the database three sessions persisted with three tasks
//		List<String> names = runner.returnVarNames();
//		List<String> ln = list("v1", "v1", "v2", "v2");
//		Collections.sort(names);
//		logger.info("names: " + names);
//		
//		assertEquals(names, ln);
//	}
	
//	@Test
//	public void storedVarMapTest() throws Exception {
//		StoredMap<UuidKey, Var> sm = runner.getViews()
//				.getVarMap();
//		
//		Iterator<Map.Entry<UuidKey, Var>> it = sm
//				.entrySet().iterator();
//				
//		List<String> names = new ArrayList<String>();
//		Map.Entry<UuidKey, Var> entry = null;
//
//		while (it.hasNext()) {
//			entry = it.next();
//			names.add(entry.getValue().getName());
//		}
//		List<String> ln = list("v1", "v1", "v2", "v2");
//		Collections.sort(names);
//		logger.info("names: " + names);
//		
//		assertEquals(names, ln);
//	}
//	
//	@Test
//	public void storedVarModelSetTest() throws Exception {
//        // get from the database three sessions persisted with three tasks
//		List<String> names = runner.returnVarModelNames();
//		List<String> ln = list("m1", "m1", "m2", "m2");
//		Collections.sort(names);
//		logger.info("names: " + names);
//		
//		assertEquals(names, ln);
//	}
//	
//	@Test
//	public void storedVarModelMapTest() throws Exception {
//		StoredMap<UuidKey, VarModel> sm = runner.getViews()
//				.getVarModelMap();
//		
//		Iterator<Map.Entry<UuidKey, VarModel>> it = sm
//				.entrySet().iterator();
//				
//		List<String> names = new ArrayList<String>();
//		Map.Entry<UuidKey, VarModel> entry = null;
//
//		while (it.hasNext()) {
//			entry = it.next();
//			names.add(entry.getValue().getName());
//		}
//		List<String> ln = list("m1", "m1", "m2", "m2");
//		Collections.sort(names);
//		logger.info("names: " + names);
//		
//		assertEquals(names, ln);
//	}
	
	@Test
	public void storedUuidObjectSetTest() throws Exception {
        // get from the database three sessions persisted with three tasks
		List<String> names = runner.returnUuidObjectNames();
		List<String> ln = list("Mike", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
	@Test
	public void storedUuidObjectMapTest() throws Exception {
		StoredMap<UuidKey, UuidObject> sm = runner.getViews()
				.getUuidObjectMap();
		
		Iterator<Map.Entry<UuidKey, UuidObject>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, UuidObject> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getObject().toString());
		}
		List<String> ln = list("Mike", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(names, ln);
	}
	
	//@Test
	public void sdbURL() throws Exception {
		URL sbdUrl = new URL("sbd://myIterface/name#context=2345");
		Object obj = sbdUrl.openConnection().getContent();
	}
}
