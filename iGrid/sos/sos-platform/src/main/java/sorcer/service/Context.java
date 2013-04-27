/*
 * Copyright 2009 the original author or authors.
 * Copyright 2009 SorcerSoft.org.
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

package sorcer.service;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.jini.id.Uuid;
import sorcer.co.tuple.Parameter;
import sorcer.core.Provider;
import sorcer.core.SorcerConstants;
import sorcer.security.util.SorcerPrincipal;
import sorcer.service.Signature.ReturnPath;

/**
 * Service dataContext classes that implement this interface provide SORCER generic
 * metacomputing data structures for storage, retrieval, and propagation of
 * heterogeneous information across all SORCER service providers. Two generic
 * implementations are provided: {@link ServiceContext} and
 * {@link ProviderContextImpl}. Usually the former is used by service requestors
 * and the latter with more functionality is used by service providers. The
 * ServiceContextImpl class implements the ProviderContext interface that
 * extends ServiceContext. An example of a specific service dataContext is
 * illustrated by {@link ArrayContext}.
 * <p>
 * A service dataContext is a tree-like structure with two types of nodes. Leaf
 * nodes are called data (value) nodes and the remaining nodes are called
 * dataContext attribute nodes. Context attributes define a namespace for the data
 * in a uniform way for use by all related services. A path of dataContext
 * attributes leading from the root of the dataContext to any leaf node along with
 * its data node is called a dataContext element (path/data). Thus a path is a
 * hierarchical attribute of data contained in the leaf node. A data node can
 * contain any Java object; in particular a generic {@see ContextNode} is
 * available.
 * <p>
 * Context paths can be marked with attributes set by using the method
 * {@link #setAttribute(String)}, which allow for efficient search of related
 * data by service providers (service dataContext clients). The search issue becomes
 * critical when a namespace in the dataContext may change or when data nodes
 * contain remote references, for example a URL. It is usually assumed that
 * provider-enforced data associations are more stable than user-oriented paths.
 * Each direct service invocation {@link Servicer.service(ServiceContext)}
 * requires data in the ServiceContext format.
 * <p>
 * Service contexts are defined by this common interface with efficient
 * service-oriented APIs. However, there are some similarities with XML
 * terminology. The root element in XML contains all other XML elements.
 * Similarly, in each service dataContext, the root extends to all data nodes. A
 * path with its data node (dataContext element) is conceptually similar to an XML
 * element. XML tags (markup) correspond to dataContext attributes. A dataContext data
 * node can be considered the analog of data in XML, however a service dataContext
 * data node has rich OO semantics. Finally, data attributes in service contexts
 * can be compared to element attributes in XML. However, data attributes have
 * more meta-attribute meaning than XML attributes. While dataContext attributes
 * provide a name space for direct access to data nodes via
 * {@link #getValue(String)}, data attributes specify the data node for indirect
 * efficient retrieval (search) by service providers. *
 */
public interface Context<T> extends Serializable, Evaluation<T>, Revaluation,
		Identifiable {

	/** dataContext parameter (cp) */
	final static String CONTEXT_PARAMETER = "cp";

	/** dataContext pipe (pipe) */
	final static String PIPE = "pipe";

	/** directional attribute (da) */
	final static String DIRECTION = "da";

	/** operand positioning (OPP) for operators (direction with index) */
	final static String OPP = "opp";

	/** index attribute (i) */
	final static String INDEX = "i";

	final static String PATH = "path";

	/** dataContext nameID (cid) */
	final static String CONTEXT_ID = "cid";

	/** SORCER type (ft) */
	final static String DATA_NODE_TYPE = "dnt";

	/** SORCER type */
	final static String VAR_NODE_TYPE = "vnt";

	final static String APPLICATION = "appl";

	final static String FORMAT = "format";

	final static String MODIFIER = "modifier";

	/** action (act) */
	final static String ACTION = "action";

	/** provider name (pn) */
	final static String PROVIDER_NAME = "pn";

	/** interface (if) */
	final static String INTERFACE = "if";

	/** selector (sl) */
	final static String SELECTOR = "sl";

	/** a variable mark */
	final static String VAR = "var";

	/** a type variable type */
	final static String VT = "vt";

	/** directional attribute values */
	final static String DA_IN = "in";

	/** directional attribute values */
	final static String DA_OUT = "out";

	/** directional attribute values */
	final static String DA_ERR = "err";
	
	/** directional attribute values */
	final static String DA_INOUT = "inout";

	/** in and out synonyms - paths equivalents */
	final static String DA_ININ = "inin";

	/** in and out synonyms - paths equivalents */
	final static String DA_OUTOUT = "outout";

	final static String SERVICE_CONTEXT = "cxt";

	final static String SERVICE_MODEL = "Service Model";

	/** EMPTY LEAF NODE i.e. node with no data and not empty string */
	final static String EMPTY_LEAF = ":Empty";

	final static String JOB = "job";

	final static String JOB_ = "job" + SorcerConstants.CPS;

	final static String TASK = "task";

	final static String TASK_ = "task" + SorcerConstants.CPS;

	final static String ID = SorcerConstants.CPS + "id";

	final static String JOB_COMMENTS = "job" + SorcerConstants.CPS + "comments";

	final static String JOB_FEEDBACK = "job" + SorcerConstants.CPS + "feedback";

	// Domain Specific Data Path
	final static String DSD_PATH = "domain" + SorcerConstants.CPS + "specific"
			+ SorcerConstants.CPS + "data";

	/**
	 * An object to specify no dataContext data.
	 */
	final static Object NO_DATA = new Object() {
		public String toString() {
			return "ServiceContext.NO_DATA";
		}
	};

	/**
	 * Returns a name of this service dataContext.
	 * 
	 * @return dataContext name
	 */
	public String getName();

	/**
	 * Assigns a name for this service dataContext.
	 * 
	 * @param name
	 *            a dataContext name to set.
	 */
	public void setName(String contextName);

	/**
	 * Returns a name of the root dataContext node.
	 * 
	 * @return name of the root dataContext node
	 */
	public String getRootName();

	/**
	 * Assigns a root name for this service dataContext.
	 * 
	 * @param rootName
	 *            name of the root dataContext node
	 */
	public void setRootName(String rootName);

	/**
	 */
	public Uuid getId();

	/**
	 * @param contextId
	 *            The identifier to set.
	 */
	public void setId(Uuid contextId);

	/**
	 */
	public String getParentPath();

	/**
	 * @param parentPath
	 *            The parentPath to set.
	 */
	public void setParentPath(String path);

	/**
	 */
	public Uuid getParentID();

	public void setParentID(Uuid parentId);

	/**
	 */
	public String getCreationDate();

	/**
	 * @param creationDate
	 *            The creationDate to set.
	 */
	public void setCreationDate(String date);

	/**
	 */
	public String getLastUpdateDate();

	/**
	 * @param lastUpdateDate
	 *            The lastUpdateDate to set.
	 */
	public void setLastUpdateDate(String date);

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String text);

	/**
	 */
	public String getDescription();

	public int getScope();

	public void setScopeCode(int scope);

	/**
	 */
	public String getOwnerID();

	/**
	 * @param ownerID
	 *            The ownerID to set.
	 */
	public void setOwnerID(String id);

	/**
	 * @param subjectID
	 *            The subjectID to set.
	 */
	public void setSubjectID(String id);

	/**
	 */
	public String getSubjectID();

	/**
	 * @param project
	 *            The project to set.
	 */
	public void setProject(String projectName);

	/**
	 */
	public String getProject();

	/**
	 * @param accessClass
	 *            The accessClass to set.
	 */
	public void setAccessClass(String acessClass);

	/**
	 */
	public String getAccessClass();

	/**
	 * @param exportControl
	 *            The exportControl to set.
	 */
	public void setExportControl(String exportControl);

	/**
	 */
	public String getExportControl();

	/**
	 */
	public String getGoodUntilDate();

	/**
	 * @param goodUntilDate
	 *            The goodUntilDate to set.
	 */
	public void setGoodUntilDate(String date);

	/**
	 */
	public String getDomainID();

	/**
	 * @param domainID
	 *            The domainID to set.
	 */
	public void setDomainID(String id);

	/**
	 */
	public String getSubdomainID();

	/**
	 * @param subdomainID
	 *            The subdomainID to set.
	 */
	public void setSubdomainID(String id);

	/**
	 */
	public String getDomainName();

	/**
	 * @param domainName
	 *            The domainName to set.
	 */
	public void setDomainName(String name);

	/**
	 */
	public String getSubdomainName();

	/**
	 * @param subdomainName
	 *            The subdomainName to set.
	 */
	public void setSubdomainName(String name);

	/**
	 * Returns a principal using this service dataContext.
	 * 
	 * @return a Principal
	 */
	public SorcerPrincipal getPrincipal();

	/**
	 * Assigns a principal to this service dataContext.
	 * 
	 * @param principal
	 *            the principal to set.
	 */
	public void setPrincipal(SorcerPrincipal principal);

	public float getVersion();

	public void setVersion(float version);

	public boolean containsPath(String path);

	public Hashtable<String, Map<String, String>> getMetacontext();

	/**
	 * @param metacontext
	 *            The metacontext to set.
	 */
	public void setMetacontext(Hashtable<String, Map<String, String>> hc);

	/**
	 * @param pathIds
	 *            The pathIds to set.
	 */
	public void setPathIds(Hashtable<String, Object> h);

	/**
	 */
	public Hashtable<String, Object> getPathIds();

	/**
	 */
	public Hashtable<String, Object> getDelPathIds();

	/**
	 * Returns the exertion associated with this dataContext.
	 * 
	 * @return Exertion
	 */
	public Exertion getExertion();

	/**
	 * Returns the service provider associated with this dataContext
	 * 
	 * @return Provider
	 */
	public Provider getProvider();

	/**
	 * Returns the path of the return value associated with this dataContext
	 * 
	 * @return The dataContext path
	 */
	public ReturnPath<T> getReturnPath();

	public T getReturnValue(Parameter... entries) throws ContextException;

	/**
	 * @param task
	 *            The task to set.
	 */
	public void setExertion(Exertion task) throws ExertionException;

	/**
	 * Returns the subject path in this dataContext. A subject is a path/value
	 * association and is interpreted as the dataContext constituent about which
	 * something is predicated. Other path/value associations of this dataContext
	 * are interpreted as complements of the dataContext subject.
	 * 
	 * @return the subject path
	 */
	public String getSubjectPath();

	/**
	 * Returns the subject value in this dataContext. A subject is a path/value
	 * association and is interpreted as the dataContext constituent about which
	 * something is predicated. Other path/value associations of this dataContext
	 * are interpreted as complements of the dataContext subject.
	 * 
	 * @return the subject value
	 */
	public Object getSubjectValue();

	/**
	 * Assigns the subject <code>value</code> at <code>path</code> in this
	 * dataContext. A subject is a path/value association and is interpreted as the
	 * dataContext constituent about which something is predicated. Other path/value
	 * associations of this dataContext are interpreted as complements of the
	 * dataContext subject.
	 * 
	 * @param path
	 *            the subject path
	 * @param value
	 *            the subject value
	 */
	public void setSubject(String path, Object value);

	public void reportException(Throwable t);

	public void reportException(String message, Throwable t);

	public void appendTrace(String footprint);

	/**
	 * Returns a value of the key object as is.
	 * 
	 * @param path
	 *            the attribute-based key
	 * @return this dataContext value
	 * @throws ContextException
	 */
	public Object get(String path);

	/**
	 * Returns an evaluated value of the key object. If the key's direct object
	 * implements the Evaluation interface then it returns getValue() of that
	 * key object, otherwise it returns the key object.
	 * 
	 * @param path
	 *            the attribute-based key
	 * @return this dataContext value
	 * @throws ContextException
	 */
	public T getValue(String path, Parameter... entries)
			throws ContextException;
	
	/**
	 * Returns an evaluated value of the key object if key exists, otherwise it
	 * return the defaultValue.
	 * 
	 * @param path
	 *            the attribute-based key
	 * @return this dataContext value
	 * @throws ContextException
	 */
	public Object getValue(String path, Object defaultValue)
			throws ContextException;

	public Object setReturnValue(Object value) throws ContextException;

	public Object putValue(String path, Object value) throws ContextException;

	public Object putValue(String path, Object value, String association)
			throws ContextException;

	public Object putLink(String path, Context cntxt, String offset)
			throws ContextException;

	public Object putLink(String name, String path, Context cntxt, String offset)
			throws ContextException;

	public Object putLink(String name, String path, String id, String offset)
			throws ContextException;

	public Object putLink(String name, String path, String lnkedCntxtID,
			float version, String offset) throws ContextException;

	public Object remove(Object path);

	/**
	 * Creates a dataContext pipe between output parameter at outPath in this
	 * dataContext to an input parameter at inPath in outContext. The pipe allow for
	 * passing on parameters between different contexts within the same
	 * exertion.
	 * 
	 * @param outPath
	 *            a location of output parameter
	 * @param inPath
	 *            a location of input parameter
	 * @param inContext
	 *            an input parameter dataContext
	 * @throws ContextException
	 */
	public void connect(String outPath, String inPath, Context inContext)
			throws ContextException;

	public void pipe(String inPath, String outPath, Context outContext)
			throws ContextException;

	/**
	 * Marks mapping between output parameter at fromParh in this dataContext to an
	 * input parameter at toPath in toContext. Mappings allow for passing on
	 * parameters between different contexts within the same exertion.
	 * 
	 * @param fromPath
	 *            a location of output parameter
	 * @param toPath
	 *            a location of input parameter
	 * @param toContext
	 *            an input parameter dataContext
	 * @throws ContextException
	 */
	public void map(String fromPath, String toPath, Context toContext)
			throws ContextException;

	/**
	 * Removes the {@link ContextLink} object pointed to by path. If object is
	 * not a dataContext link, a ContextException will be thrown.
	 * 
	 * @throws ContextException
	 * @see #removePath
	 */
	public void removeLink(String path) throws ContextException;

	/**
	 * Returns an <code>Object</code> array containing the ServiceContext in
	 * which path belongs and the absolute path in that dataContext.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @return <code>Object</code> array of length two. The first element is the
	 *         ServiceContext; the second element is the path in the
	 *         ServiceContext. Note, a dataContext node is not required at the
	 *         returned path in the returned dataContext--the results merely
	 *         indicate the mapping (getValue on the resulting dataContext at the
	 *         resulting path will yield the contents)
	 * 
	 * @throws ContextException
	 * @see #getValue
	 */
	public Object[] getContextMap(String path) throws ContextException;

	/**
	 * Records this dataContext in related monitoring session.
	 **/
	public void checkpoint() throws ContextException;

	/**
	 * Annotates the path with the tuple (value sequence) specified by a
	 * relation in the domain of attribute product related to the dataContext data
	 * nodes.The relation can be either a single search attribute (property) or
	 * attribute product. The search attribute-value sequence must be separated
	 * by the <code>Context.APS</code>. Marked data nodes can be found by their
	 * tuples (@link #getMarkedPaths} independently of associative dataContext paths
	 * defined by dataContext attributes {@link #getValue}. It is usually assumed
	 * that search relations are more stable than user friendly dataContext paths.
	 * 
	 * Tuple attributes and relations must first be registered with the dataContext
	 * with {@link #addAttribute}, {@link #addProperty}, or {@link #addRelation}
	 * before they are used in contexts.
	 * 
	 * @param path
	 *            the location in the dataContext namespace
	 * @param tuple
	 *            the domain-value sequence
	 * @return self
	 * @throws ContextException
	 */
	public Context mark(String path, String tuple) throws ContextException;

	/**
	 * Returns the enumeration of all dataContext paths matching the given
	 * association.
	 * 
	 * @param association
	 *            the association of this dataContext to be matched
	 * @return the enumeration of matches for the given association
	 * @throws ContextException
	 */
	public Enumeration<?> markedPaths(String association)
			throws ContextException;

	/**
	 * Returns the List of all values with paths matching the given association.
	 * 
	 * @param association
	 *            the association of this dataContext to be matched
	 * @return the List of matches for the given association
	 * @throws ContextException
	 */
	public List<?> getMarkedValues(String association) throws ContextException;

	/**
	 * Register an attribute with a ServiceContext's metacontext
	 * 
	 * @param attribute
	 *            name
	 */
	public void setAttribute(String descriptor) throws ContextException;

	/**
	 * Returns boolean value designating if <code>attributeName</code> is an
	 * attribute in the top-level dataContext. Does not descend into linked
	 * contexts. Is true if attribute is singleton or metaattribute type.
	 * 
	 * @see #isLocalSingletonAttribute
	 * @see #isLocalMetaattribute
	 */
	public boolean isLocalAttribute(String attributeName);

	/**
	 * Returns boolean value designating if <code>attributeName</code> is a
	 * singleton attribute in the top-level dataContext. Does not descend into
	 * linked contexts.
	 * 
	 * @see #isLocalAttribute
	 * @see #isLocalMetaattribute
	 */
	public boolean isLocalSingletonAttribute(String attributeName);

	/**
	 * Returns boolean value designating if <code>attributeName</code> is a meta
	 * attribute in the top-level dataContext. Does not descend into linked
	 * contexts.
	 * 
	 * @see #isLocalAttribute
	 * @see #isLocalSingletonAttribute
	 */
	public boolean isLocalMetaattribute(String attributeName);

	/**
	 * Returns a {@link boolean} value designating if <code>attributeName</code>
	 * is an attribute in this dataContext including any linked contexts
	 * 
	 * @return <code>boolean</code>
	 * @throws ContextException
	 * @see #isLocalAttribute
	 * @see #setSingletonAttribute
	 * @see #setMetaattribute
	 * @see #isSingletonAttribute
	 * @see #isMetaattribute
	 */
	public boolean isAttribute(String attributeName) throws ContextException;

	/**
	 * Returns a {@link boolean} value designating if <code>attributeName</code>
	 * is a singleton attribute in this dataContext including any linked contexts
	 * 
	 * @return <code>boolean</code>
	 * @throws ContextException
	 * @see #isLocalAttribute
	 * @see #setSingletonAttribute
	 * @see #setMetaattribute
	 * @see #isAttribute
	 * @see #isMetaattribute
	 */
	public boolean isSingletonAttribute(String attributeName)
			throws ContextException;

	/**
	 * Returns a {@link boolean} value designating if <code>attributeName</code>
	 * is a meta attribute in this dataContext including any linked contexts
	 * 
	 * @return <code>boolean</code>
	 * @throws ContextException
	 * @see #isLocalAttribute
	 * @see #setSingletonAttribute
	 * @see #setMetaattribute
	 * @see #isAttribute
	 * @see #isSingletonAttribute
	 */
	public boolean isMetaattribute(String attributeName)
			throws ContextException;

	/**
	 * Returns the value part of the specified attribute that has been assigned
	 * to this dataContext node. The attribute can be either a singleton attribute
	 * or a meta-attribute, which is itself a collection of attributes.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @param attributeName
	 *            the name of the metaattribute
	 * 
	 * @return <code>String</code> the attribute value
	 * @throws ContextException
	 * @see #getSingletonAttributeValue
	 * @see #getMetaattributeValue
	 */
	public String getAttributeValue(String path, String attributeName)
			throws ContextException;

	/**
	 * Returns the value part of the specified singleton attribute that has been
	 * assigned to this dataContext node. A singleton attribute is a single
	 * attribute with a single value as distinguished from a metaattribute which
	 * designates multiple attribute-value pairs.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @param attributeName
	 *            the name of the metaattribute
	 * 
	 * @return <code>String</code> the attribute value
	 * @throws ContextException
	 * @see #getAttributeValue
	 * @see #getMetaattributeValue
	 */
	public String getSingletonAttributeValue(String path, String attributeName)
			throws ContextException;

	/**
	 * Returns the value part of the specified metaattribute that has been
	 * assigned to this dataContext node. The attribute value is a concatenation of
	 * the individual attribute values, separated by the dataContext meta-path
	 * separator character (CMPS).
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @param attributeName
	 *            the name of the metaattribute
	 * 
	 * @return <code>String</code> the meta-attribute value
	 * @throws ContextException
	 * @see #getAttributeValue
	 * @see #getSingletonAttributeValue
	 */
	public String getMetaattributeValue(String path, String attributeName)
			throws ContextException;

	public void removeAttributeValue(String path, String attributeValue)
			throws ContextException;

	/**
	 * Returns the metapath for the given metaattribute in the top-level
	 * dataContext. Meta-definitions in linked contexts are not examined, and in
	 * general can be different. To examine metapaths in linked contexts, call
	 * getLocalMetapath operating on the <code>ServiceContext</code> that is
	 * linked (which can be obtained, for example, from the getDataContext method of
	 * {@link ContextLink} objects. Context links can be found using
	 * {@link getLinks}). Returns <code>null</code> if not defined.
	 * 
	 * Metapaths are set using {@link setMetaattribute}
	 * 
	 * @param metaattributeName
	 *            the name of the metaattribute
	 * 
	 * @return the metapath or <code>null</code> if not defined
	 * @throws ContextException
	 * @see #getLinks
	 * @see ContextLink
	 * @see #setMetaattribute
	 * @see #getComponentAttributes
	 */
	public String getLocalMetapath(String metaattributeName)
			throws ContextException;

	public boolean isValid(Signature method) throws ContextException;

	/**
	 * Returns an {@link Enumeration} of the locations of all the objects in
	 * this dataContext. The enumeration includes objects that reside in linked
	 * contexts.
	 * 
	 * @return <code>Enumeration</code>
	 * @throws ContextException
	 * @see ContextLink
	 * @see #getLocalLinkPaths
	 * @see #getLinks
	 */
	public Enumeration<?> contextPaths() throws ContextException;

	/**
	 * Returns the enumeration of all dataContext paths matching the given regular
	 * expression.
	 * 
	 * @param regex
	 *            the regular expression to which paths of this dataContext are to
	 *            be matched
	 * @return an enumeration of matches for the given regular expression
	 * @throws ContextException
	 */
	public Enumeration<?> paths(String regex) throws ContextException;

	/**
	 * Returns an enumeration of all values of this service dataContext.
	 * 
	 * @return an enumeration of all dataContext values
	 * @throws ContextException
	 */
	public Enumeration<?> contextValues() throws ContextException;

	/**
	 * Returns an {@link Enumeration} of the locations of the first-level
	 * {@link ContextLink} objects in this dataContext. The enumeration does not
	 * include ContextLink objects that reside in linked contexts.
	 * 
	 * @return <code>Enumeration</code>
	 * @throws ContextException
	 * @see ContextLink
	 * @see #getLinkPaths
	 */
	public Enumeration<?> localLinkPaths() throws ContextException;

	/**
	 * Returns an {@link Enumeration} of the locations of the
	 * {@link ContextLink} objects in this dataContext. The enumeration includes
	 * ContextLink objects that reside in linked contexts.
	 * 
	 * @return <code>Enumeration</code>
	 * @throws ContextException
	 * @see ContextLink
	 * @see #getLocalLinkPaths
	 * @see #getLinks
	 */
	public Enumeration<?> linkPaths() throws ContextException;

	/**
	 * Returns an {@link Enumeration} of all the {@link ContextLink} objects in
	 * this dataContext including any ContextLinks that reside in linked contexts.
	 * 
	 * @return <code>Enumeration</code>
	 * @throws ContextException
	 * @see ContextLink
	 * @see #getLocalLinks
	 * @see #getLinkPaths
	 */
	public Enumeration<?> links() throws ContextException;

	/**
	 * Returns an {@link Enumeration} of the top-level {@link ContextLink}
	 * objects in this dataContext. Does not include ContextLinks that reside in
	 * linked contexts.
	 * 
	 * @return <code>Enumeration</code>
	 * @throws ContextException
	 * @see ContextLink
	 * @see #getLinks
	 * @see #getLocalLinkPaths
	 */
	public Enumeration<?> localLinks() throws ContextException;

	/**
	 * Returns the {@link ContextLink} object that resides at path in the
	 * dataContext. This method is necessary since ContextLink objects are otherwise
	 * transparent. For example, getValue(path) returns a value in the linked
	 * dataContext, not the LinkedContext object.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @return <code>ContextLink</code> if a ContextLink object resides at path;
	 *         <code>null</code> otherwise.
	 * @throws ContextException
	 * @see ContextLink
	 */
	public Link getLink(String path) throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_IN.
	 */
	public Object putInValue(String path, Object value) throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_OUT.
	 */
	public Object putOutValue(String path, Object value)
			throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_INOUT.
	 */
	public Object putInoutValue(String path, Object value)
			throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_IN.
	 */
	public Object putInValue(String path, Object value, String association)
			throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_OUT.
	 */
	public Object putOutValue(String path, Object value, String association)
			throws ContextException;

	/**
	 * Allocates a value in the dataContext with the directional attribute set to
	 * DA_INOUT.
	 */
	public Object putInoutValue(String path, Object value, String association)
			throws ContextException;

	/**
	 * Sets directional attribute to DA_IN.
	 */
	public Context setIn(String path) throws ContextException;

	/**
	 * Sets directional attribute to DA_OUT.
	 */
	public Context setOut(String path) throws ContextException;

	/**
	 * Sets directional attribute to DA_INOUT.
	 */
	public Context setInout(String path) throws ContextException;

	/*
	 * Extensions for further dataContext operations
	 */
	public Context getSubcontext();

	public Context getSubcontext(String path) throws ContextException;

	public Context appendSubcontext(Context cntxt) throws ContextException;

	public Context appendSubcontext(Context cntxt, String path)
			throws ContextException;

	/**
	 * Removes a dataContext node from the dataContext. If the designated path points to
	 * a {@link ContextLink} object, a {@link ContextException} will be thrown.
	 * Use {@link removeLink} to remove link contexts.
	 * 
	 * @see #removeLink
	 * @throws ContextException
	 */
	public void removePath(String path) throws ContextException;

	/**
	 * Returns a string representation of this dataContext.
	 * 
	 * @return a string representation
	 */
	public String toString();

	/**
	 * Returns a plain string representation of this dataContext or in the HTML
	 * format.
	 * 
	 * @param isHTML
	 * @return a plain string if isHTML is false, otherwise in the HTML format.
	 */
	public String toString(boolean isHTML);

	/**
	 * Check if this dataContext is export controlled, accessible to principals from
	 * export controlled countries.
	 * 
	 * @return true if is export controlled
	 */
	public boolean isExportControlled();

	/**
	 * Assigns export control for this dataContext to <code>state</code> boolean.
	 * 
	 * @param state
	 *            of export control for this dataContext
	 */
	public void isExportControlled(boolean state);

	/**
	 * Returns path of first occurrence of object in the dataContext. Returns null
	 * if not found. This method is useful for orphaned objects, but should be
	 * used with caution, since the same object can appear in the dataContext in
	 * multiple places, and the location may have relevance to interpretation.
	 * If an object is orphaned, it is best to re-think how the object was
	 * obtained. It is best to refer to the object with the unique path.
	 */
	public String getPath(Object obj) throws ContextException;

	/**
	 * Returns all singleton attributes for the top-level dataContext. Does not
	 * descend into linked contexts to retrieve attributes (see
	 * {@link getSingletonAttributes} which does look in linked contexts).
	 * 
	 * @return Enumeration of singleton attributes (all of type
	 *         <code>String</code>)
	 * @see #getSingletonAttributes
	 * @see #getLocalMetaattribute
	 */
	public Enumeration<?> localSimpleAttributes();

	/**
	 * Returns all singleton attributes for the dataContext. Descends into linked
	 * contexts to retrieve underlying singleton attributes (see
	 * {@link getLocalSingletonAttributes} which does not look in linked
	 * contexts).
	 * 
	 * @return Enumeration of meta attributes (all of type <code>String</code>)
	 * @throws ContextException
	 * @see #getLocalSingletonAttributes
	 * @see #getMetaattributes
	 * @see #getAttributes
	 */
	public Enumeration<?> simpleAttributes() throws ContextException;

	/**
	 * Get all meta associations (meta attribute-meta value pairs) at the
	 * specified dataContext node.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @return Enumeration of meta associations (of type <code>String</code>)
	 * @throws ContextException
	 * @see #getAssociations
	 * @see #getAssociations(String)
	 * @see #getSingletonAssociations
	 */
	public Enumeration<?> metaassociations(String path) throws ContextException;

	/**
	 * Returns all locally defined attributes in this dataContext (metacontext).
	 * 
	 * @return Enumeration of local attributes (all of type <code>String</code>)
	 */
	public Enumeration<?> localAttributes();

	/**
	 * Returns all composite attributes for the top-level dataContext. Does not
	 * descend into linked contexts to retrieve meta attributes (see
	 * {@link getCompositeAttributes} which does look in linked contexts).
	 * 
	 * @return Enumeration of meta attributes (all of type <code>String</code>)
	 * @see #getMetaattributes
	 * @see #getLocalSingletonAttributes
	 */
	public Enumeration<?> localCompositeAttributes();

	/**
	 * Returns all meta attributes for the dataContext. Descends into linked
	 * contexts to retrieve underlying meta attributes (see
	 * {@link getLocalMetaattributes} which does not look in linked contexts).
	 * 
	 * @return Enumeration of meta attributes (all of type <code>String</code>)
	 * @throws ContextException
	 * @see #getLocalMetaattributes
	 * @see #getSingletonAttributes
	 * @see #getAttributes
	 */
	public Enumeration<?> compositeAttributes() throws ContextException;

	/**
	 * Returns all attributes (singleton and meta) for the dataContext. Descends
	 * into linked contexts to retrieve underlying attributes (see
	 * {@link getLocalMetaattributes} or {@link getLocalSingletonAttributes}
	 * which do not look in linked contexts).
	 * 
	 * @return Enumeration of attributes (all of type <code>String</code>)
	 * @throws ContextException
	 * @see #getMetaattributes
	 * @see #getSingletonAttributes
	 * @see #getLocalMetaattributes
	 * @see #getLocalSingletonAttributes
	 */
	public Enumeration<?> getAttributes() throws ContextException;

	/**
	 * Returns all attributes (simple and composite) at path in the dataContext.
	 * 
	 * @param path
	 *            the location in the dataContext
	 * @return Enumeration of attributes (all of type <code>String</code>)
	 * @throws ContextException
	 */
	public Enumeration<?> getAttributes(String path) throws ContextException;

	public Object getData();

	public int size();

	public enum Value {
		NULL, NONE, ALL, FREE
	}

	public final Value ALL = Value.ALL;
	public final Value NONE = Value.NONE;

	public enum Type {
		ASSOCIATIVE, SHARED, POSITIONAL, LIST, INDEXED, ARRAY
	}
	
	final static String PARAMETER_TYPES = "dataContext/parameter/types";
	final static String PARAMETER_VALUES = "dataContext/parameter/values/";
	final static String TARGET = "dataContext/target";
	final static String RETURN = "dataContext/result";

}
