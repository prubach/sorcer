package sorcer.util;

import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.io.MarshalledInstance;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;
import sorcer.service.Task;

import java.io.IOException;
import java.rmi.MarshalledObject;

/**
 * SORCER class
 * User: prubach
 * Date: 01.03.14
 * Created to move functionality that needs sos-platform to be outside sorcer-api
 */
public class ObjectClonerAdv extends ObjectCloner {

    public static Object cloneWithNewIDs(Object o) {
        Object obj = null;
        try {
            obj = new MarshalledObject<Object>(o).get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return renewIDs(obj);
    }

    public static Object cloneAnnotatedWithNewIDs(Object o) {
        Object obj = null;
        try {
            obj = new MarshalledInstance(o).get(false);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return renewIDs(obj);
    }


    private static Object renewIDs(Object obj) {
        if (obj instanceof Job) {
            Uuid id = UuidFactory.generate();
            ((ServiceExertion) obj).setId(UuidFactory.generate());
            for (Exertion each : ((Job) obj).getExertions()) {
                ((ServiceExertion) each).setParentId(id);
                renewIDs(each);
            }
        } else if (obj instanceof Task) {
            ((ServiceExertion) obj).setId(UuidFactory.generate());
        }
        return obj;
    }
}
