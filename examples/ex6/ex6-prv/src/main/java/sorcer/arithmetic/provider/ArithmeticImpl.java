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
package sorcer.arithmetic.provider;

import net.jini.lookup.entry.UIDescriptor;
import net.jini.lookup.ui.MainUI;
import sorcer.arithmetic.provider.ui.CalculatorUI;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.ui.serviceui.UIComponentFactory;
import sorcer.ui.serviceui.UIDescriptorFactory;
import sorcer.util.Sorcer;

import java.net.URL;
import java.rmi.RemoteException;

public class ArithmeticImpl implements Arithmetic {

//public class ArithmeticImpl implements Arithmetic, Adder {
	
	private Arithmometer arithmometer = new Arithmometer();

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.ex5.provider.Adder#add(sorcer.service.Context)
	 */
	@Override
	public Context add(Context context) throws RemoteException,
			ContextException {
		return arithmometer.add(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.ex5.provider.Subtractor#subtract(sorcer.service.Context)
	 */
	@Override
	public Context subtract(Context context) throws RemoteException,
			ContextException {
		return arithmometer.subtract(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sorcer.ex5.provider.Multiplier#multiply(sorcer.service.Context)
	 */
	@Override
	public Context multiply(Context context) throws RemoteException,
			ContextException {
		return arithmometer.multiply(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sorcer.ex5.provider#divide(sorcer.service.Context)
	 */
	@Override
	public Context divide(Context context) throws RemoteException,
			ContextException {
		return arithmometer.divide(context);
	}

    /*
	 * (non-Javadoc)
	 *
	 * @see sorcer.ex5.provider.Averager#average(sorcer.service.Context)
	 */
    @Override
    public Context average(Context context) throws RemoteException, ContextException {
        return arithmometer.average(context);    }


    public static UIDescriptor getCalculatorDescriptor() {
        UIDescriptor uiDesc = null;
        try {
            uiDesc = UIDescriptorFactory.getUIDescriptor(MainUI.ROLE,
                    new UIComponentFactory(new URL[]{new URL(Sorcer
                            .getWebsterUrl()
                            + "/calculator-ui.jar")}, CalculatorUI.class
                            .getName()));
        } catch (Exception ex) {
            // do nothing
        }
        return uiDesc;
    }
}
