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
package sorcer.core.provider.exertmonitor.db;

import java.io.File;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * SessionDatabase defines the storage containers for the ExertMonitor database.
 * 
 * @author Mike Sobolewski
 */
public class SessionDatabase {

    private static final String CLASS_CATALOG = "java_class_catalog";
    private static final String SESSION_STORE = "sesion_store";

    private Environment env;
    private Database sessionDb;
    private StoredClassCatalog javaCatalog;

    /**
     * Open all storage containers and catalogs.
     */
    public SessionDatabase(String homeDirectory)
        throws DatabaseException {
        // Open the Berkeley DB environment in transactional mode.
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        env = new Environment(new File(homeDirectory), envConfig);

        // Set the Berkeley DB config for opening all stores.
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        // Create the Serial class catalog.  This holds the serialized class
        // format for all database records of serial format.
        //
        Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
        javaCatalog = new StoredClassCatalog(catalogDb);

        // Open the Berkeley DB database for the monitor session
        // store.  The store is opened with no duplicate keys allowed.
        sessionDb = env.openDatabase(null, SESSION_STORE, dbConfig);
    }

    /**
     * Return the storage environment for the database.
     */
    public final Environment getEnvironment() {
        return env;
    }

    /**
     * Return the class catalog.
     */
    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    }

    /**
     * Return the monitor session storage container.
     */
    public final Database getSessionDatabase() {
        return sessionDb;
    }
    
    /**
     * Close all stores (closing a store automatically closes its indices).
     */
    public void close()
        throws DatabaseException {
        // Close secondary databases, then primary databases.
        sessionDb.close();
        // And don't forget to close the catalog and the environment.
        javaCatalog.close();
        env.close();
    }

}
