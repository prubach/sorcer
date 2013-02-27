/*
 * @(#)ConnectionEventListener.java	1.1 99/05/11
 * 
 * Copyright (c) 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * 
 */

package javax.sql;

/** 
<P>
A ConnectionEventListener is an object that registers to receive 
events generated by a PooledConnection.
<P>
The ConnectionEventListener interface is implemented by a
connection pooling component.  A connection pooling component will
usually be provided by a JDBC driver vendor, or another system software
vendor.  A ConnectionEventListener is notified by a JDBC driver when
an application is finished using its Connection object.  This event occurs
after the application calls close on its representation of the
PooledConnection.  A ConnectionEventListener is also notified when a
Connection error occurs due to the fact that the PooledConnection is unfit
for future use---the server has crashed, for example.  The listener is
notified, by the JDBC driver, just before the driver throws an
SQLException to the application using the PooledConnection.
*/

public interface ConnectionEventListener extends java.util.EventListener {

  /**
   * <P>Invoked when the application calls close() on its
   * representation of the connection.
   *
   * @param event an event object describing the source of 
   * the event
   */
  void connectionClosed(ConnectionEvent event);
      
  /**
   * <p>Invoked when a fatal connection error occurs, just before
   * an SQLException is thrown to the application.
   *
   * @param event an event object describing the source of 
   * the event
   */
  void connectionErrorOccurred(ConnectionEvent event);

 } 





