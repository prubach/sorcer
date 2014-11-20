package junit.sorcer.core.context.model.par;

import static sorcer.po.operator.add;
import static sorcer.po.operator.invoker;
import static sorcer.po.operator.par;
import static sorcer.po.operator.pars;

import java.rmi.RemoteException;

import sorcer.core.context.model.par.ParModel;
import sorcer.po.operator;
import sorcer.service.ContextException;

/**
 * @author Mike Sobolewski
 *
 */
@SuppressWarnings("rawtypes")
public class ParModeler {

	public static ParModel getParModel() throws
			RemoteException, ContextException {
		ParModel pm = operator.parModel("par-parModel");
		add(pm, par("x", 10.0), par("y", 20.0));
		add(pm, invoker("expr", "x + y + 30", pars("x", "y")));
		return pm;
	}

}
