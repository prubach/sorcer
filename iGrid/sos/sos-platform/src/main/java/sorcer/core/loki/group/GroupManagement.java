package sorcer.core.loki.group;

import sorcer.core.provider.proxy.RemotePartner;
import sorcer.service.Context;

import java.rmi.Remote;
import java.rmi.RemoteException;

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