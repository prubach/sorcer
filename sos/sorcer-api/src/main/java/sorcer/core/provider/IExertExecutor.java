/*
 * Copyright 2014 Sorcersoft.com S.A.
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

package sorcer.core.provider;

import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import sorcer.service.Arg;
import sorcer.service.Exertion;
import sorcer.service.ExertionException;

import java.rmi.RemoteException;

/**
 * @author Rafał Krupiński
 */
public interface IExertExecutor {
    public Exertion exert(Exertion xrt, Arg... entries) throws TransactionException,
              ExertionException, RemoteException;

    public Exertion exert(Exertion xrt, Transaction txn, Arg... entries)
            throws TransactionException, ExertionException, RemoteException;

    public Exertion exert(Exertion xrt, Transaction txn, String providerName, Arg... entries)
            throws TransactionException, ExertionException, RemoteException ;
}
