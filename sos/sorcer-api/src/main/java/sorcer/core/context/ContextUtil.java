package sorcer.core.context;
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


import net.jini.id.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.core.context.node.ContextNode;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.util.StringUtils;

import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import static sorcer.core.SorcerConstants.APS;
import static sorcer.core.SorcerConstants.CPS;

/**
 * Static fields and methods copied from Contexts. Not moved so original Contexts class is not changed. Migrate after sorcer codebases are merged.
 *
 * @author Rafał Krupiński
 */
public class ContextUtil {
    final private static Logger log= LoggerFactory.getLogger(ContextUtil.class);

    final static String SORCER_VARIABLES_PATH = "supportObjects" + CPS
            + "sorcerVariables";

    public static void copyNodes(Context fromCntxt, Context toCntxt)
            throws ContextException {
        Enumeration enu = ((Hashtable) fromCntxt).keys();
        String key;
        Object val;
        try {
            while (enu.hasMoreElements()) {
                key = (String) enu.nextElement();
                val = toCntxt.getValue(key);

                if (val instanceof ContextNode) {
                    // Util.debug(this, "old DataNode data =
                    // "+((DataNode)val).getData());
                    // Util.debug(this, "new DataNode data =
                    // "+((DataNode)fromCntxt.getValue(key)).getData());
                    ((ContextNode) val).copy((ContextNode) fromCntxt
                            .getValue(key));
                    // Util.debug(this, "old DataNode data =
                    // "+((DataNode)val).getData());
                } else if (!(key.equals(SORCER_VARIABLES_PATH))) {
                    toCntxt.putValue(key, fromCntxt.getValue(key));
                }
            }
        } catch (MalformedURLException me) {
            throw new ContextException("Caught MalformedURLException", me);
        }

        // remove sorcer variables from new context
        // these objects are new objects and collide with old
        // object IDs in original context
        ((Hashtable) toCntxt).remove(SORCER_VARIABLES_PATH);
    }

    public static ContextNode[] getContextNodes(Context context)
            throws ContextException {
        Enumeration e = context.contextPaths();
        java.util.Set nodes = new HashSet();
        Object obj = null;
        while (e.hasMoreElements()) {
            obj = e.nextElement();
            if (obj != null && obj instanceof ContextNode)
                nodes.add(obj);
        }
        ContextNode[] nodeArray = new ContextNode[nodes.size()];
        nodes.toArray(nodeArray);
        return nodeArray;
    }

    public static Object putDirectionalValue(Context context, String path,
            Object node, String attribute, String value)
            throws ContextException {
        Uuid contextID = context.getId();
        if (value == null)
            value = SorcerConstants.NULL;
        StringBuffer sb = new StringBuffer();
        sb
                .append(Context.CONTEXT_PARAMETER)
                .append(APS)
                .append(attribute)
                .append(APS)
                .append(value)
                .append(APS)
                .append(
                        contextID == null ? SorcerConstants.NULL
                                : contextID);

        if (node instanceof ContextNode)
            ((ContextNode) node).setDA(attribute);

        return context.putValue(path, value, sb.toString());
    }

    public static boolean containsContextNodeWithMetaAssoc(Context context,
            String metaAssoc) throws ContextException {
        String attr, value, key;
        Hashtable values;
        attr = metaAssoc.substring(0, metaAssoc.indexOf(APS));
        value = metaAssoc.substring(metaAssoc.indexOf(APS) + 1);
        log.debug("attr, value=" + attr + "," + value);
        if (!context.isMetaattribute(attr))
            return false;
        values = (Hashtable) context.getMetacontext().get(attr);
        log.debug("values=" + values);
        Enumeration e = values.keys();
        while (e.hasMoreElements()) {
            key = (String) e.nextElement();
            if (values.get(key).equals(value))
                if (context.get(key) instanceof ContextNode)
                    return true;
        }
        return false;
    }

    public static String[] getContextNodePaths(Context context)
			throws ContextException {
		String path;
		Vector contextNodes = new Vector();
		Enumeration e = context.contextPaths();
		while (e.hasMoreElements()) {
			path = (String) e.nextElement();
			if (context.getValue(path) instanceof ContextNode)
				contextNodes.addElement(path);
		}
		String[] contextNodePaths = new String[contextNodes.size()];
		contextNodes.copyInto(contextNodePaths);
		return contextNodePaths;
	}

    public static String getMarkerForDataNodeType(Context ctx, String path) {
        return getMarkerValueByAttribute(ctx, path, Context.DATA_NODE_TYPE);
    }

    public static String getMarkerValueByAttribute(Context ctx, String path, String attr) {
        StringBuilder markerStr = new StringBuilder();
        try {
            Hashtable hash = ctx.getMetacontext();
            if (!ctx.isMetaattribute(attr))
                return null;
            String localMeta = ctx.getLocalMetapath(attr);

            if (localMeta!=null)
                for (String loc : StringUtils.tokenize(localMeta, SorcerConstants.APS)) {
                    if ((hash!=null && !hash.isEmpty()) &&
                            ((Hashtable)hash.get(loc))!=null &&
                    (((Hashtable)hash.get(loc)).containsKey(path))) {
                        Object val = ((Hashtable)hash.get(loc)).get(path);
                        if (val!=null) markerStr.append(SorcerConstants.APS).append(val);
                    }
                }
            if (markerStr.length()>0)
                return attr + markerStr.toString();
        } catch (ContextException ce) {
            return null;
        }
        return  null;
    }
}
