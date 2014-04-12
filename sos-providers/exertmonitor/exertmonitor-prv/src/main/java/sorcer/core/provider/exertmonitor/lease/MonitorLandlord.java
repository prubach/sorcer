/*
 * Copyright 2012 the original author or authors.
 * Copyright 2012 SorcerSoft.org.
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

package sorcer.core.provider.exertmonitor.lease;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.id.ReferentUuid;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;

import com.sun.jini.landlord.Landlord;
import com.sun.jini.landlord.LeaseFactory;
import com.sun.jini.landlord.LeasedResource;

public class MonitorLandlord implements Runnable, ReferentUuid, Remote, IMonitorLandlord {

    private transient LeaseFactory lFactory;
    private transient Uuid landlordUuid;
    private transient IMonitorLandlord proxy;
	private volatile boolean run = true;

	static transient final String LOGGER = "sorcer.core.provider.monitor.lease.MonitorLandlord";
	static transient final Logger logger = Logger.getLogger(LOGGER);

	// A simple leasing policy...10 minute leases.
	protected static final int DEFAULT_MAX_LEASE = 1000 * 60 * 1;

    protected int maxLease = DEFAULT_MAX_LEASE;

	protected static final int DEFAULT_SLEEP_TIME = 1000 * 3;

	private Map<Uuid, MonitorLeasedResource> resources;

	public MonitorLandlord() throws ExportException {
		resources = new HashMap<Uuid, MonitorLeasedResource>();
		landlordUuid = UuidFactory.generate();
		export();
		this.lFactory = new LeaseFactory(proxy, landlordUuid);
	}

	public void export() throws ExportException {
		BasicJeriExporter exporter = new BasicJeriExporter(
				TcpServerEndpoint.getInstance(0), new BasicILFactory());

		proxy = (IMonitorLandlord) exporter.export(this);
		Thread llt = new Thread(this);
		llt.setDaemon(true);
		llt.start();
	}

	public IMonitorLandlord getServiceProxy() {
		return proxy;
	}

	@Override
    public Lease newLease(MonitorLeasedResource resource) {
		resources.put(resource.getCookie(), resource);
		return lFactory
				.newLease(resource.getCookie(), resource.getExpiration());
	}

	// Change the maximum lease time from the default.
	public void setMaxLease(int maxLease) {
		this.maxLease = maxLease;
	}

	// Apply the policy to a requested duration
	// to get an actual expiration time.
	public long getExpiration(long request) {
		if (request > maxLease || request == Lease.ANY)
			return System.currentTimeMillis() + maxLease;
		else
			return System.currentTimeMillis() + request;
	}

	// Cancel the lease represented by 'cookie'
	public void cancel(Uuid cookie) throws UnknownLeaseException,
			RemoteException {

		MonitorLeasedResource resource = resources.get(cookie);
		if (resource != null) {
			resource.leaseCancelled();
			return;
		}

		throw new UnknownLeaseException(cookie.toString());
	}

	// Cancel a set of leases
	public Map<Uuid, Exception> cancelAll(Uuid[] cookies) throws RemoteException {
		Map<Uuid, Exception> exceptionMap = null;

		for (int i = 0; i < cookies.length; i++) {
			try {
				cancel(cookies[i]);
			} catch (UnknownLeaseException ex) {
				if (exceptionMap == null) {
					exceptionMap = new HashMap<Uuid, Exception>();
				}
				exceptionMap.put(cookies[i], ex);
			}
		}
		return exceptionMap;
	}

	// Renew the lease specified by 'cookie'
	public long renew(Uuid cookie, long extension)
			throws UnknownLeaseException, LeaseDeniedException, RemoteException {

		MonitorLeasedResource resource;
		resource = (MonitorLeasedResource) resources.get(cookie);
		if (resource != null) {
			long expiration = getExpiration(extension);
			resource.setExpiration(expiration);
			// logger.log(Level.INFO,"Lease renewd for resource ="+resource+
			// " next lease duration="+ (expiration -
			// System.currentTimeMillis()));

			return expiration - System.currentTimeMillis();
		}
		throw new UnknownLeaseException(cookie.toString());
	}

	// Renew a set of leases.
	public Landlord.RenewResults renewAll(Uuid[] cookies, long[] extensions)
			throws RemoteException {
		long[] granted = new long[cookies.length];
		Exception[] denied = null;

		for (int i = 0; i < cookies.length; i++) {
			try {
				granted[i] = renew(cookies[i], extensions[i]);
			} catch (Exception ex) {
				if (denied == null) {
					denied = new Exception[cookies.length + 1];
				}
				denied[i + 1] = ex;
			}
		}

		Landlord.RenewResults results = new Landlord.RenewResults(granted,
				denied);
		logger.log(Level.INFO, "leases renewed Landlord.RenewResults="
				+ results);
		return results;
	}

	public void run() {
		long timeToSleep = DEFAULT_SLEEP_TIME;
		while (run) {
			long nextWakeup = System.currentTimeMillis() + timeToSleep;
			try {
				Thread.sleep(timeToSleep);
			} catch (InterruptedException ex) {
			}

			long currentTime = System.currentTimeMillis();
			// see if we're at the next wakeup time
			if (currentTime >= nextWakeup) {
				nextWakeup = currentTime + DEFAULT_SLEEP_TIME;
				// notify
				checkLeasesAndTimeouts();
			}
			timeToSleep = nextWakeup - System.currentTimeMillis();
		}
	}

	public void checkLeasesAndTimeouts() {
		// logger.log(Level.INFO,"Checking for leases and time outs");
		long now = System.currentTimeMillis();

        for (MonitorLeasedResource resource : resources.values()) {
			if (resource.getExpiration() < now) {
				logger.log(Level.INFO, "Lease cancelled for resource ="
						+ resource);
				resource.leaseCancelled();
				resources.remove(resource.getCookie());
			} else if (resource.getTimeout() < now) {
				logger.log(Level.INFO, "Timeout for resource =" + resource
						+ " resource.getTimeout()=" + resource.getTimeout()
						+ " now=" + now + " resource.getTimeout()-now="
						+ (resource.getTimeout() - now));
				resource.timedOut();
				resources.remove(resource.getCookie());
			}
		}

	}

	public void remove(LeasedResource lr) {
		logger.log(Level.INFO, "Removing landlord resource =" + lr);
		resources.remove(lr.getCookie());
	}

	public Map<Uuid, MonitorLeasedResource> getResources() {
		return resources;
	}

	public Uuid getReferentUuid() {
		return landlordUuid;
	}
	
	public void terminate() {
		run = false;
	}
}
