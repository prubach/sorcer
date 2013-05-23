package sorcer.ex3.requestor;

import sorcer.ex2.provider.InvalidWork;
import sorcer.ex2.provider.Work;
import sorcer.service.Context;
import sorcer.service.ContextException;

import java.io.Serializable;

public class Works implements Serializable {

    public static Work work1, work2, work3;

    static {
        work1 = new Work() {
            public Context exec(Context cxt) throws InvalidWork, ContextException {
                String p =  cxt.getPrefix();
                int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                cxt.putOutValue(p+"provider/result", arg1 + arg2);
                return cxt;
            }
        };

        work2 = new Work() {
            public Context exec(Context cxt) throws InvalidWork, ContextException {
                String p =  cxt.getPrefix();
                int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                cxt.putOutValue(p+"provider/result", arg1 * arg2);
                return cxt;
            }
        };

        work3 = new Work() {
            public Context exec(Context cxt) throws InvalidWork, ContextException {
                String p =  cxt.getPrefix();
                int arg1 = (Integer) cxt.getValue(p+"requestor/operand/1");
                int arg2 = (Integer) cxt.getValue(p+"requestor/operand/2");
                cxt.putOutValue(p+"provider/result", arg1 - arg2);
                return cxt;
            }
        };
    }
}
