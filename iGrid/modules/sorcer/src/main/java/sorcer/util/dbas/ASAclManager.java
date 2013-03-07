/*
 * Copyright 2010 the original author or authors.
 * Copyright 2010 SorcerSoft.org.
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

package sorcer.util.dbas;

import java.io.File;
import java.io.FileInputStream;
import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.Group;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import sorcer.core.SorcerConstants;
import sorcer.util.SorcerUtil;
import sun.security.acl.AclEntryImpl;
import sun.security.acl.AclImpl;
import sun.security.acl.GroupImpl;
import sun.security.acl.PermissionImpl;
import sun.security.acl.PrincipalImpl;

/**
 * ASAclManager class implements default ACLs that guard SQL queries execution
 * and the ApplicationServer user-access file. Custom acls might be added in
 * subclasses by implementing addAdditionalAcls method, that installs additional
 * acls in the acls hashtable that are tested by the method
 * isAuthorizedToAccessResource. When additional acls are added the
 * ApplicationServer then configuration file as.def should include the entry:
 * applicationServer.isAclExtended=true
 */
public class ASAclManager implements SorcerConstants {
	private static Logger logger = Logger.getLogger(ASAclManager.class
			.getName());
	private Permission read, write;
	private Group sqlGroup, adminGroup;
	private Acl sqlAcl, adminAcl;
	// guarded default resources
	final private String accessFile = "as.access", sqlQuery = "sqlQuery";
	final private String sqlGroupName = "Sql", adminGroupName = "Admin";
	final private String rootRoleName = ROOT, adminRoleName = ADMIN;
	private Principal root, admin;
	private static Set allPrincipals;
	private static Set allPermissions;
	protected static Hashtable acls;

	/**
	 * The constructor is responsible for setting up the sqlAcl and adminAcl.
	 * 
	 * @exception NotOwnerException
	 *                If the caller principal is not one of the owners of the
	 *                Acl.
	 */
	public ASAclManager() throws NotOwnerException {
		allPrincipals = new HashSet();
		allPermissions = new HashSet();
		root = new PrincipalImpl(rootRoleName);
		admin = new PrincipalImpl(adminRoleName);
		allPrincipals.add(root);
		allPrincipals.add(admin);

		// create default ApplicationServer permissions
		allPermissions.add(read = new PermissionImpl("read"));
		allPermissions.add(write = new PermissionImpl("write"));
		// default SQL permissions
		allPermissions.add(new PermissionImpl("select"));
		allPermissions.add(new PermissionImpl("update"));
		allPermissions.add(new PermissionImpl("insert"));
		allPermissions.add(new PermissionImpl("delete"));
		allPermissions.add(new PermissionImpl("create"));
		allPermissions.add(new PermissionImpl("drop"));
		allPermissions.add(new PermissionImpl("alter"));

		sqlGroup = new GroupImpl(sqlGroupName);
		sqlGroup.addMember(root);
		sqlGroup.addMember(admin);

		adminGroup = new GroupImpl(adminGroupName);
		adminGroup.addMember(root);
		adminGroup.addMember(admin);

		sqlAcl = new AclImpl(root, sqlQuery);
		adminAcl = new AclImpl(root, accessFile);

		// Cash all acls, in particilar those implemented in subclasses
		acls = new Hashtable();
		acls.put(sqlAcl.getName(), sqlAcl);
		acls.put(adminAcl.getName(), adminAcl);

		loadAccessConfigFile();
		addAdditionalAcls();
	}

	/**
	 * Installs additional acls in the acls hashtable that are tested by the
	 * method isAuthorizedToAccessResource. When additional acls are added the
	 * ApplicationServer configuration file as.def should include the entry:
	 * applicationServer.isAclExtended=true
	 */
	protected void addAdditionalAcls() {
		// implemented by subclasses,
		// create custom acls and add them to the existing acls:
		// acls.put(myAcl.getName(), myAcl);
	}

	/**
	 * Creates an acl for the resource resourceName and adds to the acls list
	 * 
	 * @param resourceName
	 *            The name of guarded resource by this acl
	 */
	protected static void createAclFor(String resourceName) {
		// implemented by subclasses,
		// create a custom acl and add it to the existing acls:
		// acls.put(myAcl.getName(), myAcl);
	}

	/**
	 * Deletes an acl for the resource resourceName from the acls list
	 * 
	 * @param resourceName
	 *            The name of guarded resource by this acl
	 */
	protected static void deleteAclFor(String resourceName) {
		// implemented by subclasses if needed,
		// delete an acl from the existing acls:
		// acls.remove(resourceName);
	}

	/**
	 * Adds an acl to the acls list
	 * 
	 * @param acl
	 *            The new acl
	 */
	protected static void addAcl(Acl acl) {
		acls.put(acl.getName(), acl);
	}

	public void addAcl(Hashtable acl) {
		String owner = (String) acl.get(ACL_OWNER);
		String id = (String) acl.get(ACL_ID);

		String[] objectDesc = SorcerUtil.tokenize((String) acl
				.get(ACL_FOROBJECT), SEP);

		Hashtable principalPermissions = consolidatePermissions((Hashtable) acl
				.get(ACL_ROLES), (Hashtable) acl.get(ACL_PERMISSIONS));

		Acl aclImpl = new AclImpl(getPrincipal(owner), objectDesc[ACL_OBJTYPE]
				+ SEP + objectDesc[ACL_OBJID]);
		for (Enumeration e = principalPermissions.keys(); e.hasMoreElements();) {
			String principalDesc = (String) e.nextElement();
			Principal entryPrincipal = getPrincipal(principalDesc);

			AclEntry posEntry = new AclEntryImpl(entryPrincipal);
			AclEntry negEntry = new AclEntryImpl(entryPrincipal);

			for (Iterator i = ((HashSet) principalPermissions
					.get(principalDesc)).iterator(); i.hasNext();) {
				// 0-permissionName 1-object_Type 2-Sign 3-Permission_Id
				String[] permission = SorcerUtil.tokenize((String) i.next(),
						SEP);
				if (permission.length < 4)
					return;

				if ((permission[1].equalsIgnoreCase("all") || permission[1]
						.equalsIgnoreCase(objectDesc[ACL_OBJTYPE]))) {
					if (("1").equals(permission[2]))
						posEntry
								.addPermission(new PermissionImpl(permission[0]));
					else if (("0").equals(permission[2]))
						negEntry
								.addPermission(new PermissionImpl(permission[0]));
				}
			}
			try {
				if (posEntry.permissions().hasMoreElements())
					aclImpl.addEntry(getPrincipal(owner), posEntry);
				if (negEntry.permissions().hasMoreElements()) {
					negEntry.setNegativePermissions();
					aclImpl.addEntry(getPrincipal(owner), negEntry);
				}
			} catch (NotOwnerException noe) {
			}
		}

		if (acls.get(objectDesc[ACL_OBJTYPE]) == null)
			acls.put(objectDesc[ACL_OBJTYPE], new Hashtable());
		((Hashtable) acls.get(objectDesc[ACL_OBJTYPE])).put(
				objectDesc[ACL_OBJID], aclImpl);
	}

	private Principal getPrincipal(String principalDesc) {
		Principal principal = null;
		String name = SorcerUtil.firstToken(principalDesc, SEP);

		if (CGROUP.equals(SorcerUtil.secondToken(principalDesc, SEP))) {
			principal = new GroupImpl(name);

			if (ApplicationDomain.groups.get(name) == null)
				return principal;

			Vector users = (Vector) ApplicationDomain.groups.get(name);
			for (int i = 0; i < users.size(); i++)
				((Group) principal).addMember(new PrincipalImpl((String) users
						.elementAt(i)));
		}

		else if (CUSER.equals(SorcerUtil.secondToken(principalDesc, SEP)))
			principal = new PrincipalImpl(SorcerUtil.firstToken(principalDesc,
					SEP));

		return principal;
	}

	private Hashtable consolidatePermissions(Hashtable principalRoles,
			Hashtable principalPermissions) {
		Hashtable consolidatedPermissions = new Hashtable();
		for (Enumeration e = principalRoles.keys(); e.hasMoreElements();) {
			String principalDesc = (String) e.nextElement();
			Vector roles = (Vector) principalRoles.get(principalDesc);
			for (int i = 0; i < roles.size(); i++) {
				logger.info("ApplicationDomain.roles="
						+ ApplicationDomain.roles);
				if (ApplicationDomain.roles.get((String) roles.elementAt(i)) == null)
					return consolidatedPermissions;
				Vector permissions = (Vector) ApplicationDomain.roles
						.get((String) roles.elementAt(i));
				if (permissions != null && !permissions.isEmpty()) {
					if (consolidatedPermissions.get(principalDesc) == null)
						consolidatedPermissions.put(principalDesc,
								new HashSet());

					for (int j = 0; j < permissions.size(); j++)
						((HashSet) consolidatedPermissions.get(principalDesc))
								.add(permissions.elementAt(j));
				}
			}
		}
		for (Enumeration e = principalPermissions.keys(); e.hasMoreElements();) {
			String principalDesc = (String) e.nextElement();

			if (consolidatedPermissions.get(principalDesc) == null)
				consolidatedPermissions.put(principalDesc, new HashSet());

			Vector permissions = (Vector) principalPermissions
					.get(principalDesc);

			if (consolidatedPermissions.get(principalDesc) == null)
				consolidatedPermissions.put(principalDesc, new HashSet());

			for (int i = 0; i < permissions.size(); i++)
				((HashSet) consolidatedPermissions.get(principalDesc))
						.add(permissions.elementAt(i));
		}
		return consolidatedPermissions;
	}

	public boolean isAclCached(String objType, String objId) {
		if (acls.get(objType) != null) {
			if (((Hashtable) acls.get(objType)).get(objId) != null)
				return true;
		}
		return false;
	}

	public static Acl getAcl(String id) {
		return (Acl) acls.get(id);
	}

	/**
	 * Adds a principal to the list of allPrincipals
	 * 
	 * @param principal
	 *            The new principal
	 */
	protected static void addPrincipal(Principal principal) {
		allPrincipals.add(principal);
	}

	/**
	 * Adds a permission to the list of allPermissions
	 * 
	 * @param permission
	 *            The new permission
	 */
	protected static void addPermission(Permission permission) {
		allPermissions.add(permission);
	}

	/**
	 * Determines if the principal is allowed to execute SQL Query.
	 * 
	 * @param operation
	 *            The name of SQL operation
	 * @param principalName
	 *            The name of the principal
	 * @return boolean Returns true if execution is allowed. Otherwise returns
	 *         false.
	 */
	public boolean isAuthorizedToExecuteSQL(String operation,
			String principalName) {
		logger.info("isAuthorizedToExecuteSQL:operation=" + operation
				+ ", principalName=" + principalName);
		boolean authorization = false;
		Principal user;
		Permission permission;
		// try to construct a principal object based on principalName
		user = findPrincipal(principalName);
		if (user == null) {
			System.err.println(this.getClass().getName() + ": Unknown user "
					+ principalName);
			return authorization;
		}

		permission = findPermission(operation.toLowerCase());
		if (permission == null) {
			System.err.println(this.getClass().getName()
					+ ": Unknown SQL operation " + operation);
			return authorization;
		}
		return authorization = sqlAcl.checkPermission(user, permission);
	}

	/**
	 * Determines if the principal is allowed to access the resource.
	 * 
	 * @param resourceName
	 *            The name of the resource
	 * @param operation
	 *            The name to the resource access operation (e.g., read, write)
	 * @param principalName
	 *            The name of the principal
	 * @return boolean Returns true if access is allowed. Otherwise returns
	 *         false.
	 */
	public boolean isAuthorizedToAccessResource(String resourceName,
			String operation, String principalName) {
		boolean authorization = false;
		Principal user;
		Permission permission;

		// try to construct a principal object based on principalName
		user = findPrincipal(principalName);
		if (user == null) {
			System.err.println(this.getClass().getName() + ": Unknown user "
					+ principalName);
			return authorization;
		}

		permission = findPermission(operation.toLowerCase());
		if (permission == null) {
			System.err.println(this.getClass().getName()
					+ ": Unknown SQL operation " + operation);
			return authorization;
		}

		Acl acl = (Acl) acls.get(resourceName);
		if (acl == null)
			createAclFor(resourceName);
		if (acl != null) {
			authorization = acl.checkPermission(user, permission);
		} else {
			System.err.println(this.getClass().getName()
					+ ": Unknown resourceName " + resourceName);
		}
		deleteAclFor(resourceName);
		return authorization;
	}

	public boolean isAuthorized(String principal, String objectType,
			String objectId, String operation) {

		logger.info("***principal=" + principal + " objectType=" + objectType
				+ " objectId =" + objectId + " operation=" + operation);
		if (acls.get(objectType) == null)
			return true;
		if (((Hashtable) acls.get(objectType)).get(objectId) == null)
			return true;
		Acl acl = (Acl) ((Hashtable) acls.get(objectType)).get(objectId);

		if (acl.isOwner(new PrincipalImpl(principal)))
			return true;

		logger.info("In aclManager permission = "
				+ acl.checkPermission(new PrincipalImpl(principal),
						new PermissionImpl(operation)));
		return acl.checkPermission(new PrincipalImpl(principal),
				new PermissionImpl(operation));
	}

	/**
	 * Determines if the named Principal object exists.
	 * 
	 * @param principalName
	 *            The name of the Pricipal to discover.
	 * @return Principal The discovered Principal or null if not found.
	 */
	public Principal findPrincipal(String principalName) {
		Principal principal;
		Iterator i = allPrincipals.iterator();
		while (i.hasNext()) {
			principal = (Principal) i.next();
			if (principal.getName().equals(principalName))
				return principal;
		}
		return null;
	}

	/**
	 * Determines if the named Permission object exists.
	 * 
	 * @param permission
	 *            The name of the Permission to discover.
	 * @return Permission The discovered Permission or null if not found.
	 */
	public Permission findPermission(String permissionName) {
		Permission permission;
		Iterator i = allPermissions.iterator();
		while (i.hasNext()) {
			permission = (Permission) i.next();
			if (permission.toString().equals(permissionName))
				return permission;
		}
		return null;
	}

	/**
	 * Reads user-access data from accessFile and creates acl entries for all
	 * users with given permissions listed in accessFile.
	 * 
	 * @exception NotOwnerException
	 *                If the caller principal is not one of the owners of the
	 *                Acl.
	 */
	private void loadAccessConfigFile() throws NotOwnerException {
		Properties access = new Properties();
		try {
			FileInputStream fin;
			fin = new FileInputStream(ApplicationDomain.asDir + File.separator
					+ accessFile);
			access.load(fin);
			fin.close();

			int index;
			String str, val;
			String[] tokens = null;
			Principal principal;
			Permission permission;
			AclEntry entry = null;
			Enumeration e = access.keys();
			while (e.hasMoreElements()) {
				str = (String) e.nextElement();
				val = access.getProperty(str.toLowerCase());
				tokens = SorcerUtil.tokenize(val, DELIM);
				if (str.startsWith("role")) {
					if (!tokens[0].equals(rootRoleName)
							&& !tokens[0].equals(adminRoleName)) {
						// principal is a role: tokens[0]
						principal = findPrincipal(tokens[0]);
						if (principal == null) {
							principal = new PrincipalImpl(tokens[0]);
							allPrincipals.add(principal);
						}
						sqlGroup.addMember(principal);
						// indexes 1 and 2 are for db login and password
						entry = new AclEntryImpl(principal);
						// debug(this, "principal: " + principal);
						for (int i = 3; i < tokens.length; i++) {
							permission = findPermission(tokens[i]);
							if (permission == null) {
								permission = new PermissionImpl(tokens[i]);
								allPermissions.add(permission);
							}
							entry.addPermission(permission);
							// debug(this, "entry: " + entry);
						}
					}
				}
				if (entry != null)
					sqlAcl.addEntry(root, entry);
				entry = null;
			}
		} catch (java.io.IOException e) {
			System.err
					.println("Exception reading ApplicationServer access properties: "
							+ e);
			System.exit(1);
		}
		createRootAclEntry();
		createAdminAclEntry();
	}

	/**
	 * Creates acl entries for rootRoleName with allPermissions.
	 * 
	 * @exception NotOwnerException
	 *                If the caller principal is not one of the owners of the
	 *                Acl.
	 */
	private void createRootAclEntry() throws NotOwnerException {
		Permission p;
		AclEntry entry = new AclEntryImpl(root);
		Iterator i = allPermissions.iterator();
		while (i.hasNext()) {
			p = (Permission) i.next();
			entry.addPermission(p);
		}
		adminAcl.addEntry(root, entry);
		sqlAcl.addEntry(root, entry);
	}

	/**
	 * Creates acl entries for adminRoleName with read and write permissions to
	 * accessFile.
	 * 
	 * @exception NotOwnerException
	 *                If the caller principal is not one of the owners of the
	 *                Acl.
	 */
	private void createAdminAclEntry() throws NotOwnerException {
		// allow to access access.as file for adminGroup
		AclEntry entry = new AclEntryImpl(adminGroup);
		entry.addPermission(read);
		entry.addPermission(write);
		adminAcl.addEntry(root, entry);

		// deny to access access.as file for sqlGroup
		entry = new AclEntryImpl(sqlGroup);
		entry.addPermission(read);
		entry.addPermission(write);
		entry.setNegativePermissions();
		sqlAcl.addEntry(root, entry);

		// revise permissins below
		Permission p;
		entry = new AclEntryImpl(adminGroup);
		Iterator i = allPermissions.iterator();
		while (i.hasNext()) {
			p = (Permission) i.next();
			entry.addPermission(p);
		}
		sqlAcl.addEntry(root, entry);
	}

	public static void cleanupACL(String objectType, String objectId) {
		if (acls != null && acls.get(objectType) != null && objectType != null
				&& objectId != null)
			((Hashtable) acls.get(objectType)).remove(objectId);
	}
} // end class ASAclManger

/*
 * public void addAcl(String objectDesc,String ownerDesc,Hashtable aclDesc)
 * throws NotOwnerException {
 * Util.debug(this,"addAcl objDesc="+objectDesc+" ownerDesc="
 * +ownerDesc+"aclDesc ="+aclDesc); Principal owner = new
 * PrincipalImpl(Util.firstToken(ownerDesc,GApp.sep)); Acl acl = new
 * AclImpl(owner, objectDesc);
 * 
 * 
 * 
 * for (Iterator i=((HashSet)aclDesc.get(prinName)).iterator();i.hasNext();) {
 * String permission = (String)i.next(); if (permission.endsWith("1") )
 * posEntry.addPermission(new
 * PermissionImpl(Util.firstToken(permission,GApp.sep))); else if
 * (permission.endsWith("0")) negEntry.addPermission(new
 * PermissionImpl(Util.firstToken(permission,GApp.sep))); if
 * (posEntry.permissions().hasMoreElements()) acl.addEntry(owner,posEntry); if
 * (negEntry.permissions().hasMoreElements()) {
 * negEntry.setNegativePermissions(); acl.addEntry(owner,negEntry); } } }
 * Util.debug(this,"addAcl() acl ="+acl); String objectType =
 * Util.firstToken(objectDesc,GApp.sep); if (acls.get(objectType) == null)
 * acls.put(objectType,new Hashtable());
 * 
 * 
 * ((Hashtable)acls.get(objectType)).put(Util.secondToken(objectDesc,GApp.sep),acl
 * ); Util.debug(this,"addAcl() acls="+acls); }
 */
