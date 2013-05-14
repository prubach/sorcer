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
package sorcer.tools.shell.cmds;

import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import net.jini.core.transaction.TransactionException;

import org.codehaus.groovy.control.CompilationFailedException;

import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.tools.shell.ShellStarter;
import sorcer.util.ExertProcessor;

	public class ScriptThread extends Thread {
		private String script;
		private File scriptFile;
		private Object result;
		private Object target = null;
		private GroovyShell gShell = new GroovyShell(ShellStarter.getLoader());

		public ScriptThread(String script) {
			this.script = script;
		}

		public ScriptThread(File file) {
			this.scriptFile = file;
		}

		public void run() {
			synchronized (gShell) {
				if (script != null) {
					target = gShell.evaluate(script);
				}
				else {
					try {
						target = gShell.evaluate(scriptFile);
					} catch (CompilationFailedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (target instanceof Exertion) {
				ExertProcessor esh = new ExertProcessor((Exertion) target);
				try {
					result = esh.exert();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (TransactionException e) {
					e.printStackTrace();
				} catch (ExertionException e) {
					e.printStackTrace();
				}
			} else {
				result = target;
			}
		}

		public Object getResult() {
			return result;
		}

		public Object getTarget() {
			return target;
		}
	}
