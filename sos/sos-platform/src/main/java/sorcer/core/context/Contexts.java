/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
 * Copyright 2013, 2014 Sorcersoft.com S.A.
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

package sorcer.core.context;

import java.net.MalformedURLException;
import java.util.*;

import sorcer.core.SorcerConstants;
import sorcer.core.context.node.ContextNode;
import sorcer.service.*;
import sorcer.util.StringUtils;

import static sorcer.core.SorcerConstants.*;

/**
 * The Contexts class provides utility services to ServiceContext that are
 * required by requestors such as the the SORCER graphical user interface and
 * and any complex SORCER requestor or service provider.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Contexts {
	/**
	 * Returns list of all values that are referenced by paths that start with
	 * the given <code>subpath</code> string.
	 * <p>
	 * Caution - a match does not indicate the returned results are subpaths of
	 * given path. For instance, consider context that contains paths. It is
	 * recommended to end a matched substring with the context path separator
	 * (SORCER.CPS).
	 * 
	 * <ul>
	 * <li>a/b/c
	 * <li>a/bb/d
	 * </ul>
	 * 
	 * a call to this method with path="a/b" will return both "a/b/c" and
	 * "a/bb/d" and only the first is a subpath.
	 * 
	 * @param context
	 *            ServiceContext to query
	 * @param subpath
	 *            the match string
	 * @return a Vector of context values maching a path
	 */
	public static List getValuesStartsWith(Context context, String subpath)
			throws ContextException {
		String path;
		List ids = new ArrayList();
		Enumeration e = context.contextPaths();
		while (e.hasMoreElements()) {
			path = (String) e.nextElement();
			if (path.startsWith(subpath))
				ids.add(context.getValue(path));
		}
		if (ids.size() > 0)
			return ids;
		else
			return null;
	}

	public static List<?> getNamedInValues(Context context) throws ContextException {
		List inpaths = Contexts.getNamedInPaths(context);
		if (inpaths == null) 
			return null;
		List list = new ArrayList(inpaths.size());
		for (Object path : inpaths)
			try {
				list.add(context.getValue((String) path));
			} catch (ContextException e) {
				throw new ContextException(e);
			}

		return list;
	}

    public static List<?> getNamedOutValues(Context context) throws ContextException {
        List outpaths = Contexts.getNamedOutPaths(context);
        if (outpaths == null)
            return null;
        List list = new ArrayList(outpaths.size());
        for (Object path : outpaths)
            try {
                list.add(context.getValue((String) path));
            } catch (ContextException e) {
                throw new ContextException(e);
            }

        return list;
    }

	public static List<?> getPrefixedInValues(Context context) throws ContextException {
		List inpaths = Contexts.getPrefixedInPaths(context);
		if (inpaths == null) 
			return null;
		List list = new ArrayList(inpaths.size());
		for (Object path : inpaths)
			try {
				list.add(context.getValue((String) path));
			} catch (ContextException e) {
				throw new ContextException(e);
			}

		return list;
	}

	/**
	 * 
	 * Returns list of paths that start with the given subpath string.
	 * <p>
	 * Caution - a match does not indicate the returned paths are subpaths of
	 * given path. It is recommended to end a matched substring with the context
	 * path separator (SORCER.CPS).
	 * 
	 * @param context
	 *            ServiceContext to query
	 * @param subpath
	 *            the match string
	 * @return a Vector of matching paths
	 * @throws ContextException
	 */
	public static ArrayList getKeysStartsWith(Context context, String subpath)
			throws ContextException {
		Enumeration e = context.contextPaths();
		String candidate;
		ArrayList result = new ArrayList();
		while (e.hasMoreElements()) {
			candidate = "" + e.nextElement();
			if (candidate.contains(subpath))
				result.add(candidate);
		}
		return result;
	}

	public static void map(String fromPath, Context fromContext, String toPath,
			Context toContext) throws ContextException {
		// add attributes
		// map sorcer types also
		String cp, cp0;
		cp = fromContext.getMetaattributeValue(fromPath,
				Context.CONTEXT_PARAMETER);
		if (cp == null)
			throw new ContextException(
					"no marked attribute as in, out, or inout");
		if (cp.startsWith(Context.DA_IN)) {
			cp0 = Context.DA_INOUT + cp.substring(Context.DA_IN.length());
			fromContext.mark(toPath, Context.CONTEXT_PARAMETER
					+ SorcerConstants.APS + cp0);
		} else if (cp.startsWith(Context.DA_OUT)) {
			// do nothing for now
		} else {
			markOut(fromContext, fromPath);
		}
		toContext.mark(toPath, Context.CONTEXT_PARAMETER
				+ APS + Context.DA_IN + APS
				+ fromPath + APS + fromContext.getId());
	}

	public static String getFormattedOut(Context sc, boolean isHTML) {
		// return context with outpaths
		String inoutAssoc = Context.DIRECTION + SorcerConstants.APS
				+ Context.DA_INOUT + APS;
		String outAssoc = Context.DIRECTION + SorcerConstants.APS
				+ Context.DA_OUT + APS;
		String[] outPaths = null, inoutPaths = null;
		try {
			outPaths = Contexts.getMarkedPaths(sc, outAssoc);
		} catch (ContextException ex) {
			// do nothing
		}
		try {
			inoutPaths = Contexts.getMarkedPaths(sc, inoutAssoc);
		} catch (ContextException ex) {
			// do nothing
		}
		String cr;
		if (isHTML)
			cr = "<br>";
		else
			cr = "\n";
		StringBuilder sb = new StringBuilder();
		if (outPaths != null)
			for (int i = 0; i < outPaths.length; i++) {
				sb.append(outPaths[i]).append(" = ");
				try {
					sb.append(sc.getValue(outPaths[i])).append(cr);
				} catch (ContextException ex) {
					sb.append("Unable to retrieve value").append(cr);
				}
			}
		if (inoutPaths != null)
			for (int i = 0; i < inoutPaths.length; i++) {
				sb.append(inoutPaths[i]).append(" = ");
				try {
					sb.append(sc.getValue(inoutPaths[i])).append(cr);
				} catch (ContextException ex) {
					sb.append("Unable to retrieve value").append(cr);
				}
			}
		return sb.toString();
	}

	/**
	 * Sets context type as input for a path
	 */
	public static Context markIn(Context cntxt, String path)
			throws ContextException {
		return cntxt.mark(path, Context.CONTEXT_PARAMETER
				+ APS + Context.DA_IN + APS + APS);
	}

	/**
	 * Sets context type as out for a path
	 */
	public static Context markOut(Context cntxt, String path)
			throws ContextException {
		return cntxt.mark(path, Context.CONTEXT_PARAMETER
				+ APS + Context.DA_OUT + APS + APS);
	}

	/**
	 * Sets context type as inout for a path
	 */
	public static Context markInout(Context cntxt, String path)
			throws ContextException {
		return cntxt.mark(path, Context.CONTEXT_PARAMETER
				+ APS + Context.DA_INOUT + APS + APS);
	}
	
	public static Context markOutPipe(Context cntxt, String path)
			throws ContextException {
		return cntxt
				.mark(path, Context.PIPE + APS + Context.DA_OUT + APS + APS);
	}

	public static Context markInPipe(Context cntxt, String path)
			throws ContextException {
		return cntxt.mark(path, Context.PIPE + APS
				+ Context.DA_IN + APS + APS);
	}
	
	public static Object getValueAt(Context cxt, int index)
			throws ContextException {
		String tuple = Context.INDEX + APS + index;
		Object[] objs = getMarkedValues(cxt, tuple);
		Object value = null;
		if (objs.length > 0) {
			value = objs[0];
		}
		return value;
	}
	
	public static Object[] getValuesAt(Context cxt, int index)
			throws ContextException {
		String tuple = Context.INDEX + APS + index;
		Object[] objs = getMarkedValues(cxt, tuple);
		return objs;
	}

	public static Object getInValueAt(Context cxt, int index)
			throws ContextException {
		String tuple = Context.OPP + APS + Context.DA_IN + APS + index;
		Object[] objs = getMarkedValues(cxt, tuple);
		Object value = null;
		if (objs.length > 0) {
			value = objs[0];
		}
		return value;
	}
	
	public static Object getOutValueAt(Context cxt, int index)
			throws ContextException {
		String tuple = Context.OPP + APS + Context.DA_OUT + APS + index;
		Object[] objs = getMarkedValues(cxt, tuple);
		Object value = null;
		if (objs.length > 0) {
			value = objs[0];
		}
		return value;
	}
	
	public static Object getInoutValueAt(Context cxt, int index)
			throws ContextException {
		String tuple = Context.OPP + APS + Context.DA_INOUT + APS + index;
		Object[] objs = getMarkedValues(cxt, tuple);
		Object value = null;
		if (objs.length > 0) {
			value = objs[0];
		}
		return value;
	}

	public static Object putInValueAt(Context cntxt, String path, Object value,
			int index) throws ContextException {
		cntxt.putValue(path, value);
		markIn(cntxt, path);
		cntxt.mark(path, Context.OPP + APS + Context.DA_IN + APS + index);
		return value;
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
            // Look for ContextNodes also in values and set the ContextNode's direction
            else {
                Object val = context.get((String)obj);
                if (val!= null && val instanceof ContextNode) {
                    String dire = Contexts.getDirection(context, (String)obj);

                    ((ContextNode)val).setDA(dire);
                    nodes.add(val);
                } else if (val!=null && val instanceof ContextLink) {
                    ContextLink cl = (ContextLink)val;
                    ContextNode[] cns = getContextNodes(cl.getContext());
                    for (ContextNode cn : cns)
                        nodes.add(cn);
                }
            }
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
	 * @return a list of {@link ContextNode}s.
	 * @throws ContextException
	 */
	public static ContextNode[] getAllContextNodes(Context context)
			throws ContextException {
		List<ContextNode> allNodes = null;
		List additional = null;
		try {
			allNodes = Arrays.asList(getContextNodes(context));
			for (Object obj : allNodes) {
				if (((ContextNode) obj).getData() instanceof Context) {
					additional = Arrays
							.asList(getAllContextNodes((Context) obj));
				}
                if (additional!=null && additional.size() > 0)
                    allNodes.addAll(additional);
            }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		ContextNode[] result = new ContextNode[allNodes.size()];

		allNodes.toArray(result);
		return result;
	}

	public static ContextNode[] getTaskContextNodes(Exertion task)
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

	public static ContextNode[] getMarkedConextNodes(Context sc,
			String association) throws ContextException {
		String[] paths = getMarkedPaths(sc, association);
		java.util.Set nodes = new HashSet();
		Object obj;
		for (int i = 0; i < paths.length; i++) {
			obj = sc.getValue(paths[i]);
			if (obj != null && obj instanceof ContextNode)
				nodes.add(obj);
		}
		ContextNode[] nodeArray = new ContextNode[nodes.size()];
		nodes.toArray(nodeArray);
		return nodeArray;
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

	public static boolean hasMarkedValue(Context sc, String association)
			throws ContextException {
		String[] paths = getMarkedPaths(sc, association);
        if (paths == null) return false;
        return paths.length > 0;
	}

	public static Object getMarkedValue(Context sc, String association)
			throws ContextException {
		return getMarkedValues(sc, association)[0];
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

	public static List getNamedInPaths(Context cntxt) throws ContextException {
		// get all the in and inout paths
		String cs = ((ServiceContext)cntxt).getCurrentSelector();
		if (cs != null)
			return getPrefixedInPaths(cntxt, cs);
		else
			return null;
	}
	
	public static List getPrefixedInPaths(Context cntxt) throws ContextException {
		// get all the in and inout paths
		String cp = ((ServiceContext)cntxt).getCurrentPrefix();
		if (cp != null)
			return getPrefixedInPaths(cntxt, cp);
		else 
			return null;
	}
	
	public static List getPrefixedInPaths(Context cntxt, String prefixPath) throws ContextException {
		// get all the in and inout paths
		String inAssoc = Context.DIRECTION + APS + Context.DA_IN;
		String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
		String[] inPaths = Contexts.getMarkedPaths(cntxt, inAssoc);
		String[] inoutPaths = Contexts.getMarkedPaths(cntxt, inoutAssoc);
		List list = new ArrayList();

		if (inPaths != null)
			for (int i = 0; i < inPaths.length; i++) {
				if (inPaths[i].startsWith(prefixPath))
					list.add(inPaths[i]);
			}
		if (inoutPaths != null)
			for (int i = 0; i < inoutPaths.length; i++)
				if (inoutPaths[i].startsWith(prefixPath))
					list.add(inoutPaths[i]);

		return list;
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


    public static List getNamedOutPaths(Context cntxt) throws ContextException {
        // get all the in and out paths
        return getPrefixedOutPaths(cntxt, ((ServiceContext)cntxt).getCurrentSelector());
    }

    public static List getPrefixedOutPaths(Context cntxt) throws ContextException {
        // get all the in and out paths
        return getPrefixedOutPaths(cntxt, ((ServiceContext)cntxt).getCurrentPrefix());
    }

    public static List getPrefixedOutPaths(Context cntxt, String prefix) throws ContextException {
        // get all the in and out paths
        String outAssoc = Context.DIRECTION + APS + Context.DA_OUT;
        String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
        String[] outPaths = Contexts.getMarkedPaths(cntxt, outAssoc);
        String[] inoutPaths = Contexts.getMarkedPaths(cntxt, inoutAssoc);
        List list = new ArrayList();

        if (outPaths != null)
            for (int i = 0; i < outPaths.length; i++) {
                if (outPaths[i].startsWith(prefix))
                    list.add(outPaths[i]);
            }
        if (inoutPaths != null)
            for (int i = 0; i < inoutPaths.length; i++)
                if (inoutPaths[i].startsWith(prefix))
                    list.add(inoutPaths[i]);
        return list;
    }

	/**
	 * Returns a map of all paths marked as data input.
	 * 
	 * @param cntxt
	 *            a service context
	 * @return map of all paths marked as data input
	 * @throws ContextException
	 */
	public static Map<String, String> getInPathsMap(Context cntxt)
			throws ContextException {
		// get all the in and inout paths
		String inAssoc = Context.DIRECTION + APS + Context.DA_IN;
		String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
		String[] inPaths = Contexts.getMarkedPaths(cntxt, inAssoc);
		String[] inoutPaths = Contexts.getMarkedPaths(cntxt, inoutAssoc);
		Map<String,String> inpaths = new HashMap<String, String>();

		if (inPaths != null)
			for (int i = 0; i < inPaths.length; i++)
				inpaths.put(inPaths[i], cntxt.getMetaattributeValue(inPaths[i],
						Context.CONTEXT_PARAMETER));
		if (inoutPaths != null)
			for (int i = 0; i < inoutPaths.length; i++)
				inpaths.put(inoutPaths[i], cntxt.getMetaattributeValue(
						inoutPaths[i], Context.CONTEXT_PARAMETER));
		return inpaths;
	}

	/**
	 * Returns a map of all path marked as output with corresponding
	 * associations.
	 * 
	 * @param cntxt
	 *            a service context
	 * @return map of all path marked as output with corresponding associations
	 * @throws ContextException
	 */
	public static Hashtable getOutPathsMap(Context cntxt)
			throws ContextException {
		// get all the out and inout paths
		String outAssoc = Context.DIRECTION + APS + Context.DA_OUT;
		String inoutAssoc = Context.DIRECTION + APS + Context.DA_INOUT;
		String[] outPaths = Contexts.getMarkedPaths(cntxt, outAssoc);
		String[] inoutPaths = Contexts.getMarkedPaths(cntxt, inoutAssoc);
		Hashtable inpaths = new Hashtable();

		if (outPaths != null)
			for (int i = 0; i < outPaths.length; i++)
				inpaths.put(outPaths[i], cntxt.getMetaattributeValue(
						outPaths[i], Context.CONTEXT_PARAMETER));
		if (inoutPaths != null)
			for (int i = 0; i < inoutPaths.length; i++)
				inpaths.put(inoutPaths[i], cntxt.getMetaattributeValue(
						inoutPaths[i], Context.CONTEXT_PARAMETER));
		return inpaths;
	}

	public static void copyValue(Context fromContext, String fromPath,
                                 Context toContext, String toPath) throws ContextException {
        if (fromContext.getValue(fromPath).equals(Context.none))
            throw new ContextException("Problem while piping Context: value - is NONE, from: "
                    + fromContext.getName() + fromPath + " to: " + toContext.getName() + toPath
                    + "\nFromContext: " + fromContext.toString() + "\nToContext" + toContext.toString());
        toContext.putValue(toPath, fromContext.getValue(fromPath));
    }


	public static Object putOutValue(Context cntxt, String path, Object value)
			throws ContextException {
		cntxt.putValue(path, value);
		markOut(cntxt, path);
		return value;
	}

	public static Object putInValue(Context cntxt, String path, Object value)
			throws ContextException {
		cntxt.putValue(path, value);
		markIn(cntxt, path);
		return value;
	}

	public static Object putOutValue(Context cntxt, String path, Object value,
			String association) throws ContextException {
		cntxt.putValue(path, value);
		markOut(cntxt, path);
		cntxt.mark(path, association);
		return value;
	}

	public static Object putInValue(Context cntxt, String path, Object value,
			String association) throws ContextException {
		cntxt.putValue(path, value);
		markIn(cntxt, path);
		cntxt.mark(path, association);
		return value;
	}

	public static String getContextParameterPath(String contextParameter) {
		return (contextParameter == null) ? null : StringUtils.secondToken(
				contextParameter, SEP);
	}

	public static String getContextParameterID(String contextParameter) {
		return (contextParameter == null) ? null : StringUtils.thirdToken(
				contextParameter, SEP);
	}

	public static String[] getMarkedPaths(Context cntxt, String association)
			throws ContextException {
		String attr, value, key;
		Hashtable values;
		// java 1.4.0 regex
		// Pattern p;
		// Matcher m;
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
			keysInLink = getMarkedPaths(cntxt
					.getLinkedContext(link), association);
			if (keysInLink != null)
				for (int i = 0; i < keysInLink.length; i++)
					keys.addElement(linkPath + CPS
							+ keysInLink[i]);
		}
		String[] keysArray = new String[keys.size()];
		keys.copyInto(keysArray);
		return keysArray;
	}

	public static boolean checkIfPathBeginsWith(Context context, String path)
			throws ContextException {
		List list = getKeysStartsWith(context, path);
		if (list != null && list.size() >= 1)
			return true;
		else
			return false;
	}

	public static Enumeration getSimpleAssociations(Context context, String key)
			throws ContextException {
		Object val;
		String attributeName;
		Vector values = new Vector();

		// locate the context and context path for this key
		Object[] map = context.getContextMapping(key);

		Context cntxt = (Context) map[0];
		String mappedKey = (String) map[1];

		Enumeration e = context.localSimpleAttributes();
		if (e != null) {
			while (e.hasMoreElements()) {
				attributeName = (String) e.nextElement();
				val = cntxt
						.getSingletonAttributeValue(mappedKey, attributeName);
				if (val != null)
					values.addElement(attributeName + APS + val);
			}
			return values.elements();
		} else
			return null;
	}

    public static String getDirection(Context context, String path) throws ContextException {
        Enumeration ens = Contexts.getSimpleAssociations(context, path);
        while (ens.hasMoreElements()) {
            String assoc = (String)ens.nextElement();
            if ((assoc).startsWith(Context.DIRECTION)) {
                return assoc.substring(assoc.indexOf(SEP)+1, assoc.length());
            }
        }
        return null;
    }
}
