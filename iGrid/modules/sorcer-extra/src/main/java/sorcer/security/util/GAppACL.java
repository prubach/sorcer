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

package sorcer.security.util;

import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.AclEntry;
import java.security.acl.Group;
import java.security.acl.NotOwnerException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import sorcer.core.SorcerConstants;
import sorcer.util.SorcerUtil;
import sorcer.util.dbas.ApplicationDomain;
import sun.security.acl.AclEntryImpl;
import sun.security.acl.GroupImpl;
import sun.security.acl.PermissionImpl;
import sun.security.acl.WorldGroupImpl;

public class GAppACL extends sun.security.acl.AclImpl implements ACLConvertor,
		SorcerConstants {

	/**
	 * ADD permission semantics for the following Business objects. Document :
	 * For adding versions to a document Folder : For adding documents to a
	 * folder and not sub folder to a folder.
	 **/
	public static final String ADD = CADD;
	/**
	 * DELETE permission semantics for the following Business objects. Document
	 * : For deleting versions of a document / document itself Folder : For
	 * deleting folder
	 **/
	public static final String DELETE = CDELETE;
	/**
	 * VIEW permission semantics for the following Business objects. Document :
	 * For viewing versions of a document older : For viewing contents of a
	 * folder
	 **/
	public static final String VIEW = CVIEW;
	/**
	 * UPDATE permission semantics for the following Business objects. Document
	 * : For updating meta information of a document/meta information of a
	 * Document. Folder : For updating meta information of a folder.
	 **/
	public static final String UPDATE = CUPDATE;

	/**
	 * If you want to give permissions to all users, use WORLD_GROUP as
	 * arguement to the group parameter in addGroupPermissions method.
	 **/
	public static final String WORLD_GROUP = "all";

	public String aclID;

	// This state determines if the object created is new/modified/unmodified.
	private int state = NEW;

	private Principal owner;

	/**
	 * Represents the GAppACL used by all providers for Business Objects It
	 * extends <code>sun.security.acl.AclImpl</code> which implements
	 * <code>java.security.Acl</code>. So the api is smiliar to Acl except for
	 * the followin built-in methods. Also pls note that by default if an object
	 * is guarded by ACL, and if there's no entry in the ACL, then permission
	 * will not be given for any action. If at all you want to include default
	 * permission for all users, use group name WORD_GROUP to add permissions.
	 * 
	 * @author Sekhar Soorianarayanan
	 * @version %I%, %G%
	 * @see Acl
	 * @see AclEntry
	 * @see Principal
	 * @see Group
	 * 
	 * @since JDK1.3
	 */
	public GAppACL(Principal owner, String name) {
		super(owner, name);
		this.owner = owner;
	}

	/**
	 * This method adds a set of possitive or negative permissions to a Acl for
	 * a particular group. This method can be used to add / update an existing
	 * Acl for a business object. Please note that for the group passed as
	 * arguement, the following things are assumed. 1) The group exists in the
	 * system(is created from the GUI component). 2) The principals of this
	 * group are automatically poulated when the object is persisted by the
	 * server. Untill then, this object cannot be used for checking permission
	 * for some principal. 3) If no such group exists, the server will discard
	 * this entry.
	 * 
	 * @param caller
	 *            The user who's working with the ACL. If the user is same as
	 *            owner, he can modify. Else an exception's thrown.
	 * @param group
	 *            The name of the group which exists in the system
	 * @param permissions
	 *            The set of permissions which you want to give/ deny the group.
	 *            Permissions can take the following values. 1)AclImpl.ADD
	 *            2)AclImpl.DELETE 3)AclImpl.VIEW 4)AclImpl.UPDATE. For any
	 *            other values, the system just discards the permissions.
	 * @param ispossitive
	 *            To give/deny the permissions listed.
	 * @exception NotOwnerException
	 *                thrown if a user who's not the owner of ACL tries to
	 *                modify the ACL.
	 **/
	public void addGroupPermissions(Principal caller, String group,
			String[] permissions, boolean isPossitive) throws NotOwnerException {

		if (!isOwner(caller))
			throw new NotOwnerException();

		boolean entryExists = false;

		for (Enumeration e = entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();

			Principal entryfor = entry.getPrincipal();

			if (entryfor instanceof Group && group.equals(entryfor.getName())
					&& !entry.isNegative() == isPossitive) {
				entryExists = true;
				fillEntries(entry, permissions);
				addEntry(caller, entry);
			}
		}

		if (!entryExists) {
			AclEntry entry = new AclEntryImpl();
			if (!isPossitive)
				entry.setNegativePermissions();
			Group grp = WORLD_GROUP.equals(group) ? new WorldGroupImpl("all")
					: new GroupImpl(group);
			entry.setPrincipal(grp);
			fillEntries(entry, permissions);
			addEntry(caller, entry);
		}

		setModified();

	}

	/**
	 * This method removes a set of possitive or negative permissions to a Acl
	 * for a particular group. If there is no permissions specified for the
	 * group, this method does not do anything.
	 * 
	 * @param caller
	 *            The user who's working with the ACL. If the user is same as
	 *            owner, he can modify. Else an exception's thrown.
	 * @param group
	 *            The name of the group which exists in the system
	 * @param permissions
	 *            The set of permissions which you want to remove for the group.
	 *            Permissions can take the following values. 1)AclImpl.ADD
	 *            2)AclImpl.DELETE 3)AclImpl.VIEW 4)AclImpl.UPDATE. For any
	 *            other values, the system just discards the permissions.
	 * @param ispossitive
	 *            to represent the possitive/ negative permissions
	 * @exception NotOwnerException
	 *                thrown if a user who's not the owner of ACL tries to
	 *                modify the ACL.
	 **/
	public void removeGroupPermissions(Principal caller, String group,
			String[] permissions, boolean isPossitive) throws NotOwnerException {

		if (!isOwner(caller))
			throw new NotOwnerException();
		for (Enumeration e = entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();

			Principal entryfor = entry.getPrincipal();

			if (entryfor instanceof Group && group.equals(entryfor.getName())
					&& !entry.isNegative() == isPossitive)
				removeEntryPermissions(entry, permissions);

		}

		setModified();
	}

	/**
	 * This method adds a set of possitive or negative permissions to a Acl for
	 * a particular group. This method can be used to add / update an existing
	 * Acl for a business object. Please note that for the user passed as
	 * arguement, the following things are assumed. 1) The user exists in the
	 * system(is created from the GUI component). 2) If no such user exists,
	 * then the server would discard the entry for that particular user.
	 * 
	 * @param caller
	 *            The user who's working with the ACL. If the user is same as
	 *            owner, he can modify. Else an exception's thrown.
	 * @param group
	 *            The name of the group which exists in the system
	 * @param permissions
	 *            The set of permissions which you want to give/ deny the group.
	 *            Permissions can take the following values. 1)AclImpl.ADD
	 *            2)AclImpl.DELETE 3)AclImpl.VIEW 4)AclImpl.UPDATE. For any
	 *            other values, the system just discards the permissions.
	 * @param ispossitive
	 *            To give/deny the permissions listed.
	 * @exception NotOwnerException
	 *                thrown if a user who's not the owner of ACL tries to
	 *                modify the ACL.
	 **/

	public void addUserPermissions(Principal caller, String user,
			String[] permissions, boolean isPossitive) throws NotOwnerException {

		if (!isOwner(caller))
			throw new NotOwnerException();

		boolean entryExists = false;

		for (Enumeration e = entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();

			Principal entryfor = entry.getPrincipal();

			if (!(entryfor instanceof Group) && user.equals(entryfor.getName())
					&& !entry.isNegative() == isPossitive) {
				entryExists = true;
				fillEntries(entry, permissions);
			}
		}

		if (!entryExists) {
			AclEntry entry = new AclEntryImpl();
			if (!isPossitive)
				entry.setNegativePermissions();
			entry.setPrincipal(new SorcerPrincipal(user));
			fillEntries(entry, permissions);
			addEntry(caller, entry);
		}

		setModified();
	}

	/**
	 * This method removes a set of possitive or negative permissions to a Acl
	 * for a particular user. If there is no permissions specified for the user,
	 * this method does not do anything.
	 * 
	 * @param caller
	 *            The user who's working with the ACL. If the user is same as
	 *            owner, he can modify. Else an exception's thrown.
	 * @param user
	 *            The name of the user which exists in the system
	 * @param permissions
	 *            The set of permissions which you want to remove for the group.
	 *            Permissions can take the following values. 1)AclImpl.ADD
	 *            2)AclImpl.DELETE 3)AclImpl.VIEW 4)AclImpl.UPDATE. For any
	 *            other values, the system just discards the permissions.
	 * @param ispossitive
	 *            to represent the possitive/ negative permissions
	 * @exception NotOwnerException
	 *                thrown if a user who's not the owner of ACL tries to
	 *                modify the ACL.
	 **/
	public void removeUserPermissions(Principal caller, String user,
			String[] permissions, boolean isPossitive) throws NotOwnerException {

		if (!isOwner(caller))
			throw new NotOwnerException();
		for (Enumeration e = entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();

			Principal entryfor = entry.getPrincipal();

			if (!(entryfor instanceof Group) && user.equals(entryfor.getName())
					&& !entry.isNegative() == isPossitive)
				removeEntryPermissions(entry, permissions);

		}

		setModified();
	}

	/**
	 * This method can be used if you want to clear all entries in ACL and work
	 * afresh. You may need this because an ACL migh already have been persisted
	 * and you want to start afresh by deleting all entries and populate from
	 * begining. If you use this, all the entries will be cleared. You may also
	 * use this for deleting an ACL for an object Just call on the ACL clearAll
	 * and persist it. If no entries are found, the ACL will be deleted.
	 * 
	 * @param caller
	 *            The user who's working with the ACL. If the user is same as
	 *            owner, he can modify. Else an exception's thrown.
	 * @exception NotOwnerException
	 *                thrown if a user who's not the owner of ACL tries to
	 *                modify the ACL.
	 **/
	public void clearAll(Principal caller) throws NotOwnerException {
		for (Enumeration e = entries(); e.hasMoreElements();)
			removeEntry(owner, (AclEntry) e.nextElement());
	}

	/**
	 * To fill the entry with the set of permissions
	 * 
	 * @param entry
	 *            The AclEntry to which the permissions are to be added.
	 * @param permissions
	 *            The set of permissions which has to be added to the entry
	 **/
	private void fillEntries(AclEntry entry, String[] permissions) {
		for (int i = 0; i < permissions.length; i++)
			entry.addPermission(new PermissionImpl(permissions[i]));
	}

	private void removeEntryPermissions(AclEntry entry, String[] permissions) {
		for (int i = 0; i < permissions.length; i++)
			entry.removePermission(new PermissionImpl(permissions[i]));
	}

	/**
	 *This method is for the system to manage the state of the object not to be
	 * used by the user.
	 **/
	public void setUnmodified() {
		state = UNMODIFIED;
	}

	private void setModified() {
		if (state == UNMODIFIED)
			state = MODIFIED;
	}

	/**
	 * To get the state of the Object for the persistance layer. The state can
	 * be GApp.MODIFIED / GApp.UNMODIFIED / GApp.DELETED
	 **/
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	/**
	 * This Class keeps the owner as the instance variable. This is required
	 * because the ACL API does not give us a mean to get the Principal which is
	 * a Owner. So we maintain the reference to the owner ourself. Pls note that
	 * there is only accessor and no mutator for dealing with owner.
	 **/

	public Principal getOwner() {
		return owner;
	}

	public static void main(String[] args) throws Exception {
		SorcerPrincipal owner = new SorcerPrincipal("owner");
		GAppACL acl = new GAppACL(owner, "acl");

		acl.addGroupPermissions(owner, "group1",
				new String[] { "add", "delete" }, true);
		acl.addUserPermissions(owner, "user1", new String[] { "view" }, true);

		for (Enumeration e = acl.entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();
			Principal p = entry.getPrincipal();
			if (p instanceof Group) {
				((Group) p).addMember(new SorcerPrincipal("user-of-group"));
				System.out.println(((Group) p).members());
			}
		}

		System.out.println(acl);
	}

	public Object unpack(Object obj) {
		if (obj instanceof Hashtable)
			return unpack((Hashtable) obj);
		else
			return null;
	}

	public Object pack(Object obj) {
		if (obj instanceof GAppACL)
			return pack((GAppACL) obj);
		else
			return null;
	}

	/**
	 * For Working with this command, you ned the Hashtable to contain the
	 * following ACL_OWNER -> name+type+id indexes (ACL_PNAME, ACL_PTYPE,
	 * ACL_PID) ACL_PERMISSIONS -> Each Row contain principalDesc as key and
	 * Vector as value which contain permissionDesc ACL_MODE ->
	 * DELETE/UPDATE/ADD ACL_ID -> If required
	 **/
	public static Hashtable pack(GAppACL gappACL) {
		Hashtable acl = new Hashtable();

		acl.put(ACL_OWNER, getPrincipalDesc(gappACL.getOwner()));

		Hashtable aclPermissions = new Hashtable();
		AclEntry entry;
		String entryPrincipal;
		for (Enumeration e = gappACL.entries(); e.hasMoreElements();) {
			entry = (AclEntry) e.nextElement();
			entryPrincipal = getPrincipalDesc(entry.getPrincipal());
			if (aclPermissions.get(entryPrincipal) == null)
				aclPermissions.put(entryPrincipal, new Vector());
			for (Enumeration e1 = entry.permissions(); e1.hasMoreElements();) {
				Object o = e1.nextElement();
				((Vector) aclPermissions.get(entryPrincipal))
						.addElement(new StringBuffer().append(o).append(SEP)
								.append(CALL).append(SEP).append(
										entry.isNegative() ? "0" : "1").append(
										SEP).toString());
			}
		}

		if (!aclPermissions.isEmpty())
			acl.put(ACL_PERMISSIONS, aclPermissions);

		if (gappACL.aclID != null)
			acl.put(ACL_ID, gappACL.aclID);

		return acl;
	}

	/**
	 *@param Principal
	 *           returns <name|CUSER/CGROUP|>
	 **/
	private static String getPrincipalDesc(Principal p) {
		return new StringBuffer(p.getName()).append(SEP).append(
				(p instanceof Group) ? CGROUP : CUSER).append(SEP).toString();
	}

	private static Principal getPrincipal(String principalDesc) {
		String[] tokens = SorcerUtil.tokenize(principalDesc, SEP);
		Principal p;
		if (CGROUP.equalsIgnoreCase(tokens[ACL_PTYPE])) {
			p = CALL.equalsIgnoreCase(tokens[ACL_PNAME]) ? new WorldGroupImpl(
					tokens[ACL_PNAME]) : new GroupImpl(tokens[ACL_PNAME]);
			if (ApplicationDomain.groups != null) {
				Vector groupPrincipals = (Vector) ApplicationDomain.groups
						.get(tokens[ACL_PNAME]);
				if (groupPrincipals != null)
					for (int i = 0; i < groupPrincipals.size(); i++)
						((Group) p).addMember(new SorcerPrincipal(
								(String) groupPrincipals.elementAt(i)));
			}
		} else
			p = new SorcerPrincipal(tokens[ACL_PNAME]);

		return p;
	}

	public static GAppACL unpack(Hashtable acl) {
		if (acl == null)
			return null;

		Hashtable aclpermissions = (Hashtable) acl.get(ACL_PERMISSIONS);
		if (aclpermissions == null || aclpermissions.isEmpty())
			return null;

		Principal owner = getPrincipal((String) acl.get(ACL_OWNER));
		GAppACL gappacl = new GAppACL(owner, "acl");

		String entryPrincipalDesc;
		Vector entryPrincipalPermissions;
		String[] tokens, tokens2;

		boolean isPossitive;
		for (Enumeration e = aclpermissions.keys(); e.hasMoreElements();) {
			entryPrincipalDesc = (String) e.nextElement();
			tokens = SorcerUtil.tokenize(entryPrincipalDesc, SEP);
			entryPrincipalPermissions = (Vector) aclpermissions
					.get(entryPrincipalDesc);
			for (int i = 0; i < entryPrincipalPermissions.size(); i++) {
				tokens2 = SorcerUtil.tokenize(
						(String) entryPrincipalPermissions.elementAt(i), SEP);
				try {
					if (CGROUP.equals(tokens[ACL_PTYPE]))
						gappacl.addGroupPermissions(owner, tokens[ACL_PNAME],
								new String[] { tokens2[POPERATION] }, "1"
										.equals(tokens2[PSIGN]));
					else
						gappacl.addUserPermissions(owner, tokens[ACL_PNAME],
								new String[] { tokens2[POPERATION] }, "1"
										.equals(tokens2[PSIGN]));
				} catch (NotOwnerException ne) {
					ne.printStackTrace();// cannot reach here....
				}
			}
		}

		// Now add every one in the group to all the entries.
		for (Enumeration e = gappacl.entries(); e.hasMoreElements();) {
			AclEntry entry = (AclEntry) e.nextElement();
			Principal p = entry.getPrincipal();
			if (p instanceof Group && ApplicationDomain.groups != null
					&& ApplicationDomain.groups.get(p.getName()) != null) {

				Vector groupPrincipals = (Vector) ApplicationDomain.groups
						.get(p.getName());
				for (int i = 0; i < groupPrincipals.size(); i++)
					((Group) p).addMember(new SorcerPrincipal(
							(String) groupPrincipals.elementAt(i)));
			}
		}

		System.out
				.println("....................CONVERTED TO A GAPP ACL........"
						+ gappacl);
		return gappacl;
	}

}
