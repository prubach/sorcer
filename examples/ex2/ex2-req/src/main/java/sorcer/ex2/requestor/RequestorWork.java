package sorcer.ex2.requestor;

import sorcer.ex2.provider.InvalidWork;
import sorcer.ex2.provider.Work;
import sorcer.service.Context;
import sorcer.service.ContextException;

/**
 * User: Mike Sobolewski
 * Date: 5/27/13
 * Time: 9:06 PM
 */
public class RequestorWork {

    static public Work work1 = new Work() {
        public Context exec(Context cxt) throws InvalidWork, ContextException {
            int arg1 = (Integer)cxt.getValue("requestor/operand/1");
            int arg2 = (Integer)cxt.getValue("requestor/operand/2");
            cxt.putOutValue("provider/result", arg1 + arg2);
            return cxt;
        }
    };

    static public Work work2 = new Work() {
        public Context exec(Context cxt) throws InvalidWork, ContextException {
            int arg1 = (Integer)cxt.getValue("requestor/operand/1");
            int arg2 = (Integer)cxt.getValue("requestor/operand/2");
            cxt.putOutValue("provider/result", arg1 * arg2);
            return cxt;
        }
    };

    static public Work work3 = new Work() {
        public Context exec(Context cxt) throws InvalidWork, ContextException {
            int arg1 = (Integer)cxt.getValue("requestor/operand/1");
            int arg2 = (Integer)cxt.getValue("requestor/operand/2");
            cxt.putOutValue("provider/result", arg1 - arg2);
            return cxt;
        }
    };
}
