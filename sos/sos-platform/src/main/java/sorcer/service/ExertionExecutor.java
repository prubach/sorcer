package sorcer.service;
/**
 *
 * Copyright 2013 Rafał Krupiński.
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


import net.jini.core.transaction.Transaction;
import sorcer.core.context.ControlContext;
import sorcer.util.ExertProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sorcer.service.Signature.ReturnPath;

/**
 * @author Rafał Krupiński
 */
public class ExertionExecutor {
    public static Object exec(Exertion exertion, Parameter... entries)
            throws ExertionException, ContextException {
        Exertion xrt;
        try {
            if (exertion.getClass() == Task.class) {
                if (((Task) exertion).getInnerTask() != null)
                    xrt = exert(((Task) exertion).getInnerTask(), null, entries);
                else
                    xrt = exertOpenTask(exertion, entries);
            } else {
                xrt = exert(exertion, null, entries);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExertionException(e);
        }
        ReturnPath returnPath = xrt.getDataContext().getReturnPath();
        if (returnPath != null) {
            if (xrt instanceof Task) {
                return xrt.getDataContext().getValue(returnPath.path);
            } else if (xrt instanceof Job) {
                return ((Job) xrt).getValue(returnPath.path);
            }
        } else {
            if (xrt instanceof Task) {
                return xrt.getDataContext();
            } else if (xrt instanceof Job) {
                return ((Job) xrt).getJobContext();
            }
        }
        throw new ExertionException("No return path in the exertion: "
                + xrt.getName());
    }

    public static <T extends Exertion> T exert(T input,
                                               Transaction transaction, Parameter... entries)
            throws ExertionException {
        try {
            ExertProcessor esh = new ExertProcessor(input);
            Exertion result = null;
            try {
                result = esh.exert(transaction, null, entries);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (T) result;
        } catch (Exception e) {
            throw new ExertionException(e);
        }
    }

    public static Exertion exertOpenTask(Exertion exertion,
                                         Parameter... entries) throws ExertionException {
        Exertion closedTask = null;
        List<Parameter> params = Arrays.asList(entries);
        List<Object> items = new ArrayList<Object>();
        for (Parameter param : params) {
            if (param instanceof ControlContext
                    && ((ControlContext) param).getSignatures().size() > 0) {
                List<Signature> sigs = ((ControlContext) param).getSignatures();
                ControlContext cc = (ControlContext) param;
                cc.setSignatures(null);
                Context tc = exertion.getDataContext();
                items.add(tc);
                items.add(cc);
                items.addAll(sigs);
                closedTask = TaskFactory.task(exertion.getName(), items.toArray());
            }
        }
        try {
            closedTask = closedTask.exert(entries);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExertionException(e);
        }
        return closedTask;
    }

}
