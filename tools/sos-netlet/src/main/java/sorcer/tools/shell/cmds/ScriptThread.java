/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.EmptyConfiguration;
import net.jini.core.transaction.TransactionException;
import org.codehaus.groovy.control.CompilationFailedException;
import sorcer.core.deploy.Deployment;
import sorcer.core.provider.IExertExecutor;
import sorcer.service.Accessor;
import sorcer.service.Arg;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;
import sorcer.tools.shell.RootLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.logging.Logger;

public class ScriptThread extends Thread {
		private String script;
		private File scriptFile;
		private Object result;
		private Object target = null;
		private GroovyShell gShell;
        private Configuration config;
        private boolean debug = false;

        private final static Logger logger = Logger.getLogger(ScriptThread.class
                .getName());

        public ScriptThread(String script, URL[] jarsToAdd, ClassLoader classLoader, PrintStream out, Configuration config, boolean debug) {
            this.config = config;
            this.debug = debug;
            RootLoader loader = null;
            if (classLoader==null) {
                loader = new RootLoader(jarsToAdd, this.getClass().getClassLoader());
                if (out!=null) out.println("New Script classloader: " + printUrls(loader.getURLs()));
            }
            else if (classLoader instanceof RootLoader) {
                loader = (RootLoader)classLoader;
                for (URL url : jarsToAdd)
                    loader.addURL(url);
                if (debug && out!=null) out.println("Existing Script classloader: " + printUrls(loader.getURLs()));
            } else if (classLoader instanceof URLClassLoader) {
                loader = new RootLoader(jarsToAdd, classLoader);
                if (debug && out!=null) out.println("Existing Script classloader: " + printUrls(loader.getURLs()));
            }
            gShell = new GroovyShell(loader!=null ? loader : classLoader);
			this.script = script;
            this.parseScript();
		}

        public ScriptThread(String script, URL[] jarsToAdd, ClassLoader classLoader, PrintStream out, Configuration config) {
            this(script, jarsToAdd, classLoader, out, config, false);
        }


        public ScriptThread(String script, URL[] urls, ClassLoader classLoader, PrintStream out) {
            this(script, urls, classLoader, out, EmptyConfiguration.INSTANCE);
        }

        public ScriptThread(String script, URL[] jarsToAdd, PrintStream out) {
            this(script, jarsToAdd, null, out);
        }

        public ScriptThread(String script, URL[] jarsToAdd) {
            this(script, jarsToAdd, null, null);
        }

        public ScriptThread(String script, ClassLoader classLoader) {
            this.gShell = new GroovyShell(classLoader);
            this.script = script;
            this.parseScript();
        }

		public ScriptThread(File file, ClassLoader classLoader) {
            this.gShell = new GroovyShell(classLoader);
			this.scriptFile = file;
            this.parseScript();
		}

        public void parseScript() {
            synchronized (gShell) {
                if (script != null) {
                    target = gShell.evaluate(script);
                }
            }
        }

		public void run() {
            synchronized (gShell) {
                if (script != null) {
                    target = gShell.evaluate(script);
                } else {
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
                Arg[] entries = new Arg[0];
                if (((Exertion) target).isProvisionable() && config != null) {
                    try {
                        String configFile = (String) config.getEntry(
                                "sorcer.tools.shell.NetworkShell",
                                "exertionDeploymentConfig", String.class,
                                null);
                        if (configFile != null)
                            entries = new Arg[]{new Deployment(configFile)};
                    } catch (ConfigurationException ignore) {
                    }
                }

                try {
                    IExertExecutor exertExecutor = Accessor.getService(IExertExecutor.class);
                    result = exertExecutor.exert((Exertion) target, entries);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (TransactionException e) {
                    e.printStackTrace();
                } catch (ExertionException e) {
                    e.printStackTrace();
                }
            }
		}

		public Object getResult() {
			return result;
		}

		public Object getTarget() {
			return target;
		}

        public String printUrls(URL[] urls) {
            StringBuilder sb = new StringBuilder("URLs: [");
            for (URL url : urls) {
                sb.append("\n").append(url.toString());
            }
            sb.append(" ]");
            return sb.toString();
        }

	}
