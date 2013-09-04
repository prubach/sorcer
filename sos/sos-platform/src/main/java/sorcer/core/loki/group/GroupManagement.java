/**
 *
 * Copyright 2013 the original author or authors.
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
package sorcer.core.loki.group;

import java.rmi.*;
import sorcer.core.provider.proxy.*;
import sorcer.service.*;

/**
 * The group management interface lays the interface for database
 * interaction, wrapping dataContext passing to send and retrieve information
 * to the database
 * 
 * @author Daniel Kerr
 */

public interface GroupManagement extends Remote, RemotePartner
{
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * get the provider identification information
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getProviderID (Context context) throws RemoteException;
	
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * execute update
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context executeUpdate(Context context) throws RemoteException;
	/**
	 * execute query
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context executeQuery(Context context) throws RemoteException;
	
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * add activity entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addActivityEntry(Context context) throws RemoteException;
	/**
	 * add exectuion entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addExecutionEntry(Context context) throws RemoteException;
	/**
	 * add exertion entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addExertionEntry(Context context) throws RemoteException;
	/**
	 * add group entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addGroupEntry(Context context) throws RemoteException;
	/**
	 * add member entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addMemberEntry(Context context) throws RemoteException;
	/**
	 * add membership entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context addMembershipEntry(Context context) throws RemoteException;

	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * get all groups
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getGroups(Context context) throws RemoteException;
	/**
	 * get group exertions
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getGroupExertions(Context context) throws RemoteException;
	/**
	 * get group members
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getGroupMembers(Context context) throws RemoteException;
	/**
	 * get group action
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getGroupAction(Context context) throws RemoteException;
	/**
	 * get action info
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getActionInfo(Context context) throws RemoteException;
	
	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * get activity entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getActivityEntry(Context context) throws RemoteException;
	/**
	 * get exertion entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getExertionEntry(Context context) throws RemoteException;
	/**
	 * get group entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getGroupEntry(Context context) throws RemoteException;
	/**
	 * get member entry
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getMemberEntry(Context context) throws RemoteException;

	//------------------------------------------------------------------------------------------------------------
	
	/**
	 * get all activities
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getActivities(Context context) throws RemoteException;
	/**
	 * get all executions
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getExecutions(Context context) throws RemoteException;
	/**
	 * get all exertions
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getExertions(Context context) throws RemoteException;
	/**
	 * get all members
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getMembers(Context context) throws RemoteException;
	/**
	 * get all memberships
	 * 
	 * @param context		information dataContext
	 * @return				results dataContext
	 */
	public Context getMemberships(Context context) throws RemoteException;
	
	//------------------------------------------------------------------------------------------------------------
}