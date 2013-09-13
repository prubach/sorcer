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
import sorcer.core.SorcerConstants;
import sorcer.core.context.node.ContextNode;
import sorcer.service.Context;
import sorcer.service.ContextException;
import sorcer.service.Exertion;
import sorcer.service.Job;
import sorcer.service.ServiceExertion;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static sorcer.core.SorcerConstants.APS;

/**
 * @author Rafał Krupiński
 */
public class ContextUtil {
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
                } else if (!(key.equals(Contexts.SORCER_VARIABLES_PATH))) {
                    toCntxt.putValue(key, fromCntxt.getValue(key));
                }
            }
        } catch (MalformedURLException me) {
            throw new ContextException("Caught MalformedURLException", me);
        }

        // remove sorcer variables from new context
        // these objects are new objects and collide with old
        // object IDs in original context
        ((Hashtable) toCntxt).remove(Contexts.SORCER_VARIABLES_PATH);
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

    /**
	 * Returns all context nodes recursively in this context and all its emebded
	 * contexts, tasks, and jobs.
	 *
	 * @param context
	 *            a servcie context
	 * @return a list -f {@link sorcer.core.context.node.ContextNode}.
	 * @throws sorcer.service.ContextException
	 */
	public static ContextNode[] getAllContextNodes(Context context)
			throws ContextException {
		List allNodes = null;
		List additional = null;
		try {
			allNodes = Arrays.asList(getContextNodes(context));
			for (Object obj : allNodes) {
				if (((ContextNode) obj).getData() instanceof Context) {
					additional = Arrays
							.asList(getAllContextNodes((Context) obj));
					if (additional.size() > 0)
						allNodes.addAll(additional);
				} else if (obj instanceof ServiceExertion) {
					additional = Arrays
							.asList(getTaskContextNodes((ServiceExertion) obj));
				} else if (obj instanceof Job) {
					additional = Arrays
							.asList(getTaskContextNodes((ServiceExertion) obj));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		ContextNode[] result = new ContextNode[allNodes.size()];

		allNodes.toArray(result);
		return result;
	}

    public static ContextNode[] getTaskContextNodes(ServiceExertion task)
			throws ContextException {
		List allNodes = new ArrayList();
		List additional = null;

		additional = Arrays.asList(getAllContextNodes(task.getContext()));
		if (additional.size() > 0)
			allNodes.addAll(additional);
		ContextNode[] result = new ContextNode[allNodes.size()];

		allNodes.toArray(result);
		return result;
	}

    public static ContextNode[] getTaskContextNodes(Job job)
			throws ContextException {
		List allNodes = new ArrayList();
		List additional = null;

		List<Exertion> exertions = job.getExertions();
		for (Object exertion : exertions) {
			if (exertion instanceof ServiceExertion) {
				additional = Arrays
						.asList(getTaskContextNodes((ServiceExertion) exertion));
				if (additional.size() > 0)
					allNodes.addAll(additional);
			} else if (exertion instanceof Job) {
				additional = Arrays.asList(getTaskContextNodes((Job) exertion));
				if (additional.size() > 0)
					allNodes.addAll(additional);
			}
		}
		ContextNode[] result = new ContextNode[allNodes.size()];

		allNodes.toArray(result);
		return result;
	}

    public static ContextNode[] getContextNodesWithAttribute(Context sc,
			String attribute) throws ContextException {
		String[] paths = Contexts.getPathsWithAttribute(sc, attribute);
		java.util.Set nodes = new HashSet();
		Object obj = null;
		for (int i = 0; i < paths.length; i++) {
			obj = sc.getValue(paths[i]);
			if (obj != null && obj instanceof ContextNode)
				nodes.add(obj);
		}
		ContextNode[] nodeArray = new ContextNode[nodes.size()];
		nodes.toArray(nodeArray);
		return nodeArray;
	}

    public static ContextNode[] getMarkedConextNodes(Context sc,
			String association) throws ContextException {
		String[] paths = Contexts.getMarkedPaths(sc, association);
		java.util.Set nodes = new HashSet();
		Object obj = null;
		for (int i = 0; i < paths.length; i++) {
			obj = sc.getValue(paths[i]);
			if (obj != null && obj instanceof ContextNode)
				nodes.add(obj);
		}
		ContextNode[] nodeArray = new ContextNode[nodes.size()];
		nodes.toArray(nodeArray);
		return nodeArray;
	}

    public static ContextNode getMarkedConextNode(Context sc, String association)
			throws ContextException {
		return getMarkedConextNodes(sc, association)[0];
	}

    public static void copyContextNodesFrom(Context toContext,
            Context fromContext) throws ContextException {
        // copy all sorcerNodes from fromContext to this context.
        for (Enumeration e = fromContext.contextPaths(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (fromContext.getValue(key) instanceof ContextNode)
                toContext.putValue(key, fromContext.getValue(key));
        }
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
        System.out.println("attr, value" + attr + "," + value);
        if (!context.isMetaattribute(attr))
            return false;
        values = (Hashtable) context.getMetacontext().get(attr);
        System.out.println("values" + values);
        Enumeration e = values.keys();
        while (e.hasMoreElements()) {
            key = (String) e.nextElement();
            if (values.get(key).equals(value))
                if (context.get(key) instanceof ContextNode)
                    return true;
        }
        return false;
    }

    public static String[] getContextNodePathsWithAssoc(Context context,
			String association) throws ContextException {
		Vector contextNodes = new Vector();
		String[] paths = Contexts.getMarkedPaths(context, association);
		if (paths == null)
			return null;
		for (int i = 0; i < paths.length; i++)
			if (context.getValue(paths[i]) instanceof ContextNode)
				contextNodes.addElement(paths[i]);
		String[] contextNodePaths = new String[contextNodes.size()];
		contextNodes.copyInto(contextNodePaths);
		return contextNodePaths;
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
}
