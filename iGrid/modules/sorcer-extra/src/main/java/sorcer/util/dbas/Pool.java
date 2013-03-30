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

import java.util.Stack;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Implements Thread Pooling. Thread Pool simply keeps a bunch of suspended
 * threads around to do some work.
 */
public class Pool {
	private static final Logger logger = Logger.getLogger(Pool.class.getName());

	/**
	 * Handler class for perform work requested by the Pool.
	 */
	class WorkerThread extends Thread {
		private Worker worker;
		private Object data;

		/**
		 * Creates a new WorkerThread
		 * 
		 * @param id
		 *            Thread ID
		 * @param worker
		 *            Worker instance associated with the WorkerThread
		 */
		WorkerThread(String id, Worker worker) {
			super(id);
			this.worker = worker;
			data = null;
		}

		/**
		 * Wakes the thread and does some work
		 * 
		 * @param data
		 *            Data to send to the Worker
		 * @return void
		 */
		synchronized void wake(Object data) {
			this.data = data;
			notify();
		}

		/**
		 * WorkerThread's thread routine
		 */
		synchronized public void run() {
			boolean stop = false;
			while (!stop) {
				if (data == null) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						continue;
					}
				}
				if (data != null) {
					worker.run(data);
				}
				data = null;
				stop = !(push(this));
			}
		}
	};

	private Stack waiting;
	private Vector running;
	private Class workerClass;

	// The Maximum threads our system will manage
	private int capacity;
	// The size of pool in waiting stack
	private int poolSize;

	/**
	 * Creates a new Pool instance
	 * 
	 * @param max
	 *            Max number of handler threads
	 * @param workerClass
	 *            Name of Worker implementation
	 * @throws Exception
	 */
	public Pool(int poolSize, Class workerClass) throws Exception {
		this.poolSize = poolSize;
		waiting = new Stack();
		this.workerClass = workerClass;
		running = new Vector();

		Worker work;
		WorkerThread wt;
		for (int i = 0; i < poolSize; i++) {
			work = (Worker) workerClass.newInstance();
			wt = new WorkerThread("Worker#" + i, work);
			wt.start();
			waiting.push(wt);
		}
		capacity = 100;
	}

	public Pool(int poolSize, int capacity, Class workerClass) throws Exception {
		this(poolSize, workerClass);
		if (capacity < poolSize)
			throw new Exception("pool size cannot be more than capacity");
		this.capacity = capacity;
	}

	/**
	 * Request the Pool to perform some work.
	 * 
	 * @param data
	 *            Data to give to the Worker
	 * @return void
	 * @throws InstantiationException
	 *             Thrown if additional worker can't be created
	 */
	public void performWork(Object data) throws InstantiationException,
			ExceededPoolCapacityException {
		WorkerThread wt = null;
		synchronized (waiting) {
			if (waiting.empty()) {

				if (running.size() == capacity)
					throw new ExceededPoolCapacityException("Pool maxed out :"
							+ running.size());

				try {
					wt = new WorkerThread("additional worker",
							(Worker) workerClass.newInstance());
					logger.finer("additional worker of type: " + workerClass);
					wt.start();
				} catch (Exception e) {
					throw new InstantiationException(
							"Problem creating instance of Worker.class: "
									+ e.getMessage());
				}
			} else {
				wt = (WorkerThread) waiting.pop();
			}
		}

		running.add(wt);
		wt.wake(data);
	}

	/**
	 * Convenience method used by WorkerThread to put Thread back on the stack
	 * 
	 * @param w
	 *            WorkerThread to push
	 * @return boolean true if pushed, false otherwise
	 */
	private boolean push(WorkerThread wt) {
		boolean stayAround = false;
		synchronized (waiting) {
			if (waiting.size() < poolSize) {
				stayAround = true;
				waiting.push(wt);
			}
		}
		running.remove(wt);

		return stayAround;
	}

	public void cleanup() {
		synchronized (running) {
			while (running.size() > 0)
				((Thread) running.remove(0)).interrupt();
		}
	}

	public static class ExceededPoolCapacityException extends Exception {
		public ExceededPoolCapacityException(String cause) {
			super(cause);
		}
	}

}
