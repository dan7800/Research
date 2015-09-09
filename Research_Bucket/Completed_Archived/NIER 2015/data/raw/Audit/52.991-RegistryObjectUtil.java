/*
 * ====================================================================
 * 
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2003 freebxml.org.  All rights reserved.
 * 
 * ====================================================================
 */
package com.sun.xml.registry.client.util;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

/*
 * Miscellaneous utility methods for debugging and tests
 *
 * $Header: /cvsroot/sino/jaxr/src/com/sun/xml/registry/client/util/RegistryObjectUtil.java,v 1.1 2003/03/20 12:10:04 ritzmann Exp $
 *
 */
public class RegistryObjectUtil {

	/**
	 * Retrieves id from RegistryObject.
	 * 
	 * @param obj
	 * @return
	 * @throws JAXRException
	 */
	public static String toId(Object obj) throws JAXRException {
		if (obj instanceof RegistryObject) {
			RegistryObject ro = (RegistryObject) obj;
			return ro.getKey().getId();
		} else {
			return "[toId: not a RegistryObject, obj=" + obj + "]";
		}
	}

	/**
	 * Retrieves first object from a Collection.
	 * 
	 * @param col
	 * @return
	 */
	public static Object getFirstObject(Collection col) {
		if (col == null) {
			return null;
		}
		Iterator it = col.iterator();
		if (!it.hasNext()) {
			return null;
		}
		return it.next();
	}

	/**
	 * Throws exception if BulkResponse contains any exceptions.
	 * 
	 * @param response
	 * @throws JAXRException
	 */
	public static void checkBulkResponse(BulkResponse response)
		throws JAXRException {
		Collection exes = response.getExceptions();
		if (exes == null) {
			return;
		}
		throw new JAXRException((JAXRException) getFirstObject(exes));
	}

	/**
	 * Retrieves owner of RegistryObject.
	 * 
	 * @param ro RegistryObject to get the owner of
	 * @return owner, ie. creator or null if this is a new RegistryObject
	 */
	public static User getOwner(RegistryObject ro) throws JAXRException {
		// Ask server who our creator is
		Collection events = ro.getAuditTrail();
		if (events == null) {
			return null;
		}
		for (Iterator it = events.iterator(); it.hasNext();) {
			AuditableEvent ev = (AuditableEvent) it.next();
			if (ev.getEventType() == AuditableEvent.EVENT_TYPE_CREATED) {
				return ev.getUser();
			}
		}
		return null;
	}

}
