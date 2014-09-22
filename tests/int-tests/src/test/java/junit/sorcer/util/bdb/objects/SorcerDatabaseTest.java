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

import static org.junit.Assert.assertEquals;
import static sorcer.co.operator.bag;
import static sorcer.co.operator.list;
import static sorcer.core.requestor.ServiceRequestor.setCodeBaseByArtifacts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RMISecurityManager;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import sorcer.junit.ExportCodebase;
import sorcer.junit.SorcerClient;
import sorcer.junit.SorcerRunner;
import sorcer.service.Context;
import sorcer.service.Exertion;
import sorcer.util.IOUtils;

import sorcer.util.ModelTable;
import sorcer.util.bdb.objects.UuidKey;
import sorcer.util.bdb.objects.UuidObject;

import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.DatabaseException;

/**
 * @author Mike Sobolewski
 */

@RunWith(SorcerRunner.class)
@Category(SorcerClient.class)
@ExportCodebase({"org.sorcersoft.sorcer:sorcer-api"})
public class SorcerDatabaseTest {

	private final static Logger logger = LoggerFactory
			.getLogger(SorcerDatabaseTest.class.getName());
	
	private static SorcerDatabaseRunner runner;
	private static File dbDir;
	
	@BeforeClass 
	public static void setUpOnce() throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String homeDir = tempDir + "/ju-sorcer-db";
		dbDir = new File(homeDir);
        IOUtils.deleteDir(dbDir);
        //System.out.println("Sorcer DB dir: " + dbDir.getCanonicalPath());
		dbDir.mkdirs();
		runner = new SorcerDatabaseRunner(homeDir);
        runner.run();
	}
	
	@AfterClass 
	public static void cleanup() throws Exception {
		// delete database home directory and close database
		IOUtils.deleteDir(dbDir);
		if (runner != null) {
            try {
                // Always attempt to close the database cleanly.
                runner.close();
            } catch (Exception e) {
                System.err.println("Trying again to close database: " + e.getMessage());
                Thread.sleep(1000);
                runner.close();
            }
        }
	}
	
	@Test
	public void storedcContextSetTest() throws Exception {
        // get from the database three contexts persisted   
		List<String> names = runner.returnContextNames();
		List<String> ln = list("c1", "c1", "c2", "c2", "c3", "c3");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
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
		List<String> ln = list("c1", "c1", "c2", "c2", "c3", "c3");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
	}
	
	@Test
	public void storedTableSetTest() throws Exception {
		// the second run and the second db population
        runner.run();
        // get from the database three tables persisted twice
		List<String> names = runner.returnTableNames();
		List<String> ln = list("undefined0", "undefined1", "undefined2", "undefined3", "undefined4", "undefined5");
		Collections.sort(names);
		logger.info("table names: " + names);
		
		assertEquals(ln, names);
	}
	
	@Test
	public void storedTableMapTest() throws Exception {
		StoredMap<UuidKey, ModelTable> sm = runner.getViews()
				.getTableMap();
		
		Iterator<Map.Entry<UuidKey, ModelTable>> it = sm
				.entrySet().iterator();
				
		List<String> names = new ArrayList<String>();
		Map.Entry<UuidKey, ModelTable> entry = null;

		while (it.hasNext()) {
			entry = it.next();
			names.add(entry.getValue().getName());
		}
		List<String> ln = list("undefined0", "undefined1", "undefined2");
		Collections.sort(names);
		logger.info("table names: " + names);
		
		assertEquals(ln, names);
	}
	
	@Test
	public void storedExertionSetTest() throws Exception {
        // get from the database two exertions persisted twice
		List<String> names = runner.returnExertionNames();
		List<String> ln = list("f1", "f1", "f4", "f4");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
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
		List<String> ln = list("f1", "f1", "f4", "f4");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
	}
	
	@Test
	public void storedUuidObjectSetTest() throws Exception {
        // get from the database three sessions persisted with three tasks
		List<String> names = runner.returnUuidObjectNames();
		List<String> ln = list("Mike", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
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
		List<String> ln = list("Mike", "Mike", "Sobolewski", "Sobolewski");
		Collections.sort(names);
		logger.info("names: " + names);
		
		assertEquals(ln, names);
	}
	
	//@Test
	public void sdbURL() throws Exception {
		URL sbdUrl = new URL("sbd://myIterface/name#context=2345");
		Object obj = sbdUrl.openConnection().getContent();
	}
}
