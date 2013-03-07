/*
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

package sorcer.util.dbas;

import java.sql.Connection;
import java.sql.SQLWarning;

import javax.naming.Context;

import sorcer.util.Stopwatch;

// This class will attempt to remake the JDK connection with 
// the application database every sleepTime seconds (by default).
public class RemakeDBConnection extends Thread {
	boolean connectionState = false, isInterrupted = false;
	Connection jdbcConnection;
	int sleepTime = 3000, timeout = 15000;
	Context context;
	String bindName;

	// Takes Context and bindName
	public RemakeDBConnection(Context context, String bindName) {
		this.context = context;
		this.bindName = bindName;
		start();
	}

	// The run method
	// This is the thread body that is attempting every sleepTime seconds
	// to remake the connection.
	public void run() {
		// Attempt to remake the connection
		Stopwatch stopwatch = new Stopwatch();
		while (!isInterrupted) {
			// Attempt to connect to a database
			try {
				if ((jdbcConnection != null))
					jdbcConnection.close();
				jdbcConnection = JdbcConnectionImpl.getConnection(context,
						bindName);
				SQLWarning warning = null;
				try {
					warning = jdbcConnection.getWarnings();
					if (warning == null)
						System.out
								.println("No SQL warnings while connected to database");
					while (warning != null) {
						System.out.println("SQL Warning: " + warning);
						warning = warning.getNextWarning();
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				connectionState = true;
				break;
			} catch (Exception e) {
				System.err.println("Still not able to connect to database");
			}

			if (stopwatch.get() > timeout) {
				// allow to exit RemakeDBConnection thread gracefully
				isInterrupted = true;
				connectionState = false;
				System.err
						.println("Made five attempts, not able to connect to database ");
				break;
			}

			// Still here, let's wait awhile (sleepTime)
			try {
				sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // end while
		if (isInterrupted) {
			System.err.println("RemakeDBConnection:time-out: "
					+ stopwatch.get());
		} else
			System.err.println("RemakeDBConnection:successful: "
					+ stopwatch.get());
	}
}
