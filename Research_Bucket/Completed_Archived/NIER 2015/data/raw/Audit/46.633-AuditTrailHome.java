/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface AuditTrailHome extends EJBHome
{
	public AuditTrail create(AuditTrailObject mObject) throws RemoteException, CreateException;

	public AuditTrail findByPrimaryKey(Long pkid) throws FinderException, RemoteException;

	public Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws RemoteException;
	
	//20080109 Jimmy - add branch and filter
	public Vector getAuditTrailReport(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status,
			String checkUserBranch, Integer iBranch) throws RemoteException;
	/*
	 * public void remove() throws RemoteException;
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface AuditTrailHome extends EJBHome
{
	public AuditTrail create(AuditTrailObject mObject) throws RemoteException, CreateException;

	public AuditTrail findByPrimaryKey(Long pkid) throws FinderException, RemoteException;

	public Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws RemoteException;
	/*
	 * public void remove() throws RemoteException;
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface AuditTrailHome extends EJBHome
{
	public AuditTrail create(AuditTrailObject mObject) throws RemoteException, CreateException;

	public AuditTrail findByPrimaryKey(Long pkid) throws FinderException, RemoteException;

	public Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws RemoteException;
	/*
	 * public void remove() throws RemoteException;
	 */
}
/*==========================================================
 *
 * Copyright  of Vincent Lee. All Rights Reserved.
 * (http://www.vlee.net)
 *
 * This software is the proprietary information of VLEE,
 * Use is subject to license terms.
 *
 ==========================================================*/
package com.vlee.ejb.user;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface AuditTrailHome extends EJBHome
{
	public AuditTrail create(AuditTrailObject mObject) throws RemoteException, CreateException;

	public AuditTrail findByPrimaryKey(Long pkid) throws FinderException, RemoteException;

	public Vector getValueObjectsGiven(Integer iUserId, String namespace, Integer iAuditType, Integer iAuditLevel,
			String keyword, Timestamp dateFrom, Timestamp dateTo, String state, String status) throws RemoteException;
	/*
	 * public void remove() throws RemoteException;
	 */
}
