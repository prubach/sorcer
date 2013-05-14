package sorcer.core.dispatch;

import sorcer.core.Dispatcher;
import sorcer.core.Provider;
import sorcer.service.ContextException;
import sorcer.service.ExecState;
import sorcer.service.Job;

import java.rmi.RemoteException;
import java.util.logging.Logger;

public class JobThread extends Thread {
	private final static Logger logger = Logger.getLogger(JobThread.class
			.getName());

	private static final int SLEEP_TIME = 250;
	// doJob method calls this internally
	private Job job;

	private Job result;

	Provider provider;

	public JobThread(Job job, Provider provider) {
		this.job = job;
		this.provider = provider;
	}

	public void run() {
		logger.finer("*** Exertion dispatcher started with control dataContext ***\n"
				+ job.getControlContext());
		Dispatcher dispatcher = null;
		try {
			dispatcher = ExertionDispatcherFactory.getFactory()
					.createDispatcher(job, provider);
			try {
				job.getControlContext().appendTrace(provider.getProviderName() +
						" dispatcher: " + dispatcher.getClass().getName());
			} catch (RemoteException e) {
				// ignore it, locall call
			}
			// int COUNT = 1000;
			// int count = COUNT;
			while (dispatcher.getState() != ExecState.DONE
					&& dispatcher.getState() != ExecState.FAILED
					&& dispatcher.getState() != ExecState.SUSPENDED) {
				// count--;
				// if (count < 0) {
				// logger.finer("*** Jobber's Exertion Dispatcher waiting in state: "
				// + dispatcher.getState());
				// count = COUNT;
				// }
				Thread.sleep(SLEEP_TIME);
			}
			logger.finer("*** Dispatcher exit state = " + dispatcher.getClass().getName()  + " state: " + dispatcher.getState()
					+ " for job***\n" + job.getControlContext());
		} catch (DispatcherException de) {
			de.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		result = (Job) dispatcher.getExertion();
	}

	public Job getJob() {
		return job;
	}

	public Job getResult() throws ContextException {
		return result;
	}
}
