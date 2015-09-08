/*
 * $Id: otsEvents.java 5462 2005-08-05 18:35:48Z jonesde $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.opentravelsystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.string.FlexibleStringExpander;
// import org.ofbiz.webapp.stats.VisitHandler;
// import org.ofbiz.webapp.control.LoginWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
//import org.ofbiz.party.contact.ContactHelper;
//import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.security.Security;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

/**
 * otsEvents - Events for the Opentravelsystem.
 *
 * @author     <a href="mailto:support@opentravelsystem.org">Hans Bakker</a>
 * @version    $Rev: 5462 $
 * @since      2.0
 */
public class otsEvents {

    public static final String module = otsEvents.class.getName();
    public static final String resource = "opentravelsystemUiLabels";


    /**
     * An HTTP WebEvent handler that checks to see if a productStore is selected.
     * If not check if the user has access to more than one store. If so redirect to the
     * store selection screen, otherwise if only access to one store, select the store,
     * If no access to any store show an error message and logoff the user.
     *
     * @param request The HTTP request object for the current JSP or Servlet request.
     * @param response The HTTP response object for the current JSP or Servlet request.
     * @return
     */
    public static String checkProductStore(HttpServletRequest request, HttpServletResponse response) {
        String productStoreId = (String) request.getSession().getAttribute("productStoreId");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        HttpSession session = request.getSession();
        java.sql.Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

        Debug.logInfo("Starting service for party:" + userLogin.getString("partyId") + " and productStoreId: " + productStoreId,module);

        // product store already selected.
        if (productStoreId != null) {
            Debug.logInfo("ProductStore already selected:" + productStoreId, module);
//            request.getSession().setAttribute("productStoreId",productStoreId);
//            request.getSession().setAttribute("webSiteId",productStoreId);
//            request.getSession().setAttribute("prodCatalogId",productStoreId);
            return "success";
        }

        // if admin always need selection of stores
        if ("admin".equals(userLogin.getString("userLoginId")))	{
            return "selectStore";
        }
        
        GenericValue productStoreRole = null;
        List productStoreRoles = null;
        // productstore not selected, check to see to which stores the user has access.
        try {
            productStoreRoles = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreRole",UtilMisc.toMap("partyId",userLogin.getString("partyId"),"roleTypeId","ADMIN")), nowTimestamp, "fromDate", "thruDate", true);
        } catch (GenericEntityException e) {
            // no stores: show error message and redirect to logoff screen.
            String excMsg = "Could not find any stores you have access to, loggin off.....";
            Debug.logError(excMsg, module);
            return "error";
        }
        // check if one store, select the store
        if (productStoreRoles != null && productStoreRoles.size() == 1) {
            Iterator firstOne = productStoreRoles.iterator();
            productStoreRole = (GenericValue) firstOne.next();
            productStoreId = productStoreRole.getString("productStoreId");
            request.getSession().setAttribute("productStoreId",productStoreId);
            request.getSession().setAttribute("webSiteId",productStoreId);
            request.getSession().setAttribute("prodCatalogId",productStoreId);
            Debug.logInfo("ProductStore selected for party:" + userLogin.getString("partyId") + " and productStoreId: " + productStoreId,module);
            return "success";
        }
        // check if many stores: goto store selection screen
        if (productStoreRoles != null && productStoreRoles.size() > 1) {
            return "selectStore";
        }
        // we should never arrive here...
        return "error";
    }

}