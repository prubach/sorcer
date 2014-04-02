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
import sorcer.service.Link;
import sorcer.util.StringUtils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
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
        toCntxt.remove(SORCER_VARIABLES_PATH);
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
                            hash.get(loc) !=null &&
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

    public static boolean hasMarkedValue(Context sc, String association)
            throws ContextException {
        String[] paths = getMarkedPaths(sc, association);
        if (paths.length == 0)
            return false;
        return true;
    }

    public static String[] getMarkedPaths(Context cntxt, String association)
            throws ContextException {
        String attr, value, key;
        Hashtable values;
        // java 1.4.0 regex
        // Pattern p;
        // Matcher m;
        boolean result;
        if (association == null)
            return null;
        int index = association.indexOf(APS);
        if (index < 0)
            return null;

        attr = association.substring(0, index);
        value = association.substring(index + 1);
        if (!cntxt.isAttribute(attr))
            throw new ContextException("No Attribute defined: " + attr);

        Vector keys = new Vector();
        if (cntxt.isSingletonAttribute(attr)) {
            values = (Hashtable) cntxt.getMetacontext().get(attr);
            if (values != null) { // if there are no attributes set,
                // values==null;
                Enumeration e = values.keys();
                while (e.hasMoreElements()) {
                    key = (String) e.nextElement();
					/*
					 * java 1.4.0 regex p = Pattern.compile(value); m =
					 * p.matcher((String)values.get(key)); if (m.find())
					 * keys.addElement(key);
					 */
                    if (values.get(key).equals(value))
                        keys.addElement(key);
                }
            }
        } else {
            // it is a metaattribute
            String metapath = cntxt.getLocalMetapath(attr);
            if (metapath != null) {
                String[] attrs = StringUtils.tokenize(metapath,
                        APS);
                String[] vals = StringUtils.tokenize(value, APS);
                if (attrs.length != vals.length)
                    throw new ContextException("Invalid association: \""
                            + association + "\"  metaattribute \"" + attr
                            + "\" is defined with metapath =\"" + metapath
                            + "\"");
                String[][] paths = new String[attrs.length][];
                int ii = -1;
                for (int i = 0; i < attrs.length; i++) {
                    paths[i] = getMarkedPaths(cntxt, attrs[i]
                            + APS + vals[i]);
                    if (paths[i] == null) {
                        ii = -1;
                        break; // i.e. no possible match
                    }
                    if (ii < 0 || paths[i].length > paths[ii].length) {
                        ii = i;
                    }
                }

                if (ii >= 0) {
                    // The common paths across the paths[][] array are
                    // matches. Said another way, the paths[][] array
                    // contains all the paths that match attributes in the
                    // metapath. paths[0][] are the matches for the first
                    // element of the metapath, paths[1][] for the next,
                    // etc. Therefore, the matches that are common for
                    // each element of the metapath are the ones in which
                    // we have interest.
                    String candidate;
                    int match, thisMatch;
                    // go through each element of one with most matches
                    for (int i = 0; i < paths[ii].length; i++) {
                        candidate = paths[ii][i];
                        // now look for paths.length-1 matches...
                        match = 0;
                        for (int j = 0; j < paths.length; j++) {
                            if (j == ii)
                                continue;
                            thisMatch = 0;
                            for (int k = 0; k < paths[j].length; k++)
                                if (candidate.equals(paths[j][k])) {
                                    match++;
                                    thisMatch++;
                                    break;
                                }
                            if (thisMatch == 0)
                                break; // no possible match for this candidate
                        }
                        // System.out.println("candidate="+candidate+"
                        // match="+match+" required maches="+(paths.length-1));
                        if (match == paths.length - 1)
                            keys.addElement(candidate);
                    }
                }
            }
        }
        // above we just checked the top-level context; next, check
        // all the top-level LINKED contexts (which in turn will check
        // all their top-level linked contexts, etc.)
        Enumeration e = cntxt.localLinkPaths();
        Link link;
        String keysInLink[], linkPath;
        while (e.hasMoreElements()) {
            linkPath = (String) e.nextElement();
            link = (Link) cntxt.get(linkPath);
            keysInLink = getMarkedPaths((cntxt).getLinkedContext(link), association);
            if (keysInLink != null)
                for (int i = 0; i < keysInLink.length; i++)
                    keys.addElement(linkPath + CPS
                            + keysInLink[i]);
        }
        String[] keysArray = new String[keys.size()];
        keys.copyInto(keysArray);
        return keysArray;
    }

    /**
     * Returns a list of all paths marked as data output.
     *
     * @param cntxt
     *            a service context
     * @return list of all paths marked as data output
     * @throws ContextException
     */
    public static List<String> getOutPaths(Context cntxt) throws ContextException {
        // get all the in and inout paths
        String outAssoc = Context.DIRECTION + APS + Context.DA_OUT;
        String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
        String[] outPaths = getMarkedPaths(cntxt, outAssoc);
        String[] inoutPaths = getMarkedPaths(cntxt, inoutAssoc);
        int cap = (outPaths == null ? 0 : outPaths.length) + (inoutPaths == null ? 0 : inoutPaths.length);
        List<String> list = new ArrayList<String>(cap);

        if (outPaths != null)
            Collections.addAll(list, outPaths);
        if (inoutPaths != null)
            Collections.addAll(list, inoutPaths);
        return list;
    }

    /**
     * Returns a list of all paths marked as data input.
     *
     * @param cntxt
     *            a service context
     * @return list of all paths marked as input
     * @throws ContextException
     */
    public static List<String> getInPaths(Context cntxt) throws ContextException {
        // get all the in and inout paths
        String inAssoc = Context.DIRECTION + APS + Context.DA_IN;
        String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
        String[] inPaths = getMarkedPaths(cntxt, inAssoc);
        String[] inoutPaths = getMarkedPaths(cntxt, inoutAssoc);
        int cap = (inPaths == null ? 0 : inPaths.length) + (inoutPaths == null ? 0 : inoutPaths.length);
        List<String> list = new ArrayList<String>(cap);

        if (inPaths != null)
            Collections.addAll(list, inPaths);
        if (inoutPaths != null)
            Collections.addAll(list, inoutPaths);
        return list;
    }

    public static Object getMarkedValue(Context sc, String association)
            throws ContextException {
        return getMarkedValues(sc, association)[0];
    }

    public static Object[] getMarkedValues(Context sc, String association)
            throws ContextException {
        String[] paths = getMarkedPaths(sc, association);
        List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < paths.length; i++) {
            values.add(sc.getValue(paths[i]));
        }
        return values.toArray();
    }

}
