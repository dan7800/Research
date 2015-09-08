/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.order.shoppingcart;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.product.ProductPromoWorker;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.config.ProductConfigWorker;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.product.store.ProductStoreSurveyWrapper;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.security.Security;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.control.RequestHandler;
// Begin neogia specific import
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import org.ofbiz.product.product.developed.Product;
import org.ofbiz.product.product.developed.ProductServices;
// End neogia specific import

/**
 * Shopping cart events.
 */
public class ShoppingCartEvents {

    public static String module = ShoppingCartEvents.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";

    private static final String NO_ERROR = "noerror";
    private static final String NON_CRITICAL_ERROR = "noncritical";
    private static final String ERROR = "error";

    public static String addProductPromoCode(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        String productPromoCodeId = request.getParameter("productPromoCodeId");
        if (UtilValidate.isNotEmpty(productPromoCodeId)) {
            String checkResult = cart.addProductPromoCode(productPromoCodeId, dispatcher);
            if (UtilValidate.isNotEmpty(checkResult)) {
                request.setAttribute("_ERROR_MESSAGE_", checkResult);
                return "error";
            }
        }
        return "success";
    }
    
    public static String addItemGroup(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        Map parameters = UtilHttp.getParameterMap(request);
        String groupName = (String) parameters.get("groupName");
        String parentGroupNumber = (String) parameters.get("parentGroupNumber");
        String groupNumber = cart.addItemGroup(groupName, parentGroupNumber);
        request.setAttribute("itemGroupNumber", groupNumber);
        return "success";
    }
    
    public static String addCartItemToGroup(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        Map parameters = UtilHttp.getParameterMap(request);
        String itemGroupNumber = (String) parameters.get("itemGroupNumber");
        String indexStr = (String) parameters.get("lineIndex");
        int index = Integer.parseInt(indexStr);
        ShoppingCartItem cartItem = cart.findCartItem(index);
        cartItem.setItemGroup(itemGroupNumber, cart);
        return "success";
    }
    
    /** Event to add an item to the shopping cart. */
    public static String addToCart(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective = null;
        Map result = null;
        String productId = null;
        String parentProductId = null;
        String itemType = null;
        String itemDescription = null;
        String productCategoryId = null;
        String priceStr = null;
        Double price = null;
        String quantityStr = null;
		//Begin Neogia Specific : add uom price and quantity management
        String uomQuantityStr = null;
        double uomQuantity = 0;
 		//End Neogia Specific : add uom price and quantity management
        double quantity = 0;
        String reservStartStr = null;
        String reservEndStr = null;
        java.sql.Timestamp reservStart = null;
        java.sql.Timestamp reservEnd = null;
        String reservLengthStr = null;
        Double reservLength = null;
        String reservPersonsStr = null;
        Double reservPersons = null;
        String shipBeforeStr = null;
        String shipBeforeDateStr = null;
        String shipAfterDateStr = null;
        java.sql.Timestamp shipBeforeDate = null;
        java.sql.Timestamp shipAfterDate = null;

        // not used right now: Map attributes = null;
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        Locale locale = UtilHttp.getLocale(request);
        NumberFormat nf = NumberFormat.getNumberInstance(locale);

        // Get the parameters as a MAP, remove the productId and quantity params.
        Map paramMap = UtilHttp.getParameterMap(request);

        String itemGroupNumber = (String) paramMap.get("itemGroupNumber");

        // Get shoppingList info if passed
        String shoppingListId = (String) paramMap.get("shoppingListId");
        String shoppingListItemSeqId = (String) paramMap.get("shoppingListItemSeqId");
        if (paramMap.containsKey("ADD_PRODUCT_ID")) {
            productId = (String) paramMap.remove("ADD_PRODUCT_ID");
        } else if (paramMap.containsKey("add_product_id")) {
            productId = (String) paramMap.remove("add_product_id");
        }
        if (paramMap.containsKey("PRODUCT_ID")) {
            parentProductId = (String) paramMap.remove("PRODUCT_ID");
        } else if (paramMap.containsKey("product_id")) {
            parentProductId = (String) paramMap.remove("product_id");
        }

        Debug.logInfo("adding item product " + productId,module);
        Debug.logInfo("adding item parent product " + parentProductId,module);

        if (paramMap.containsKey("ADD_CATEGORY_ID")) {
            productCategoryId = (String) paramMap.remove("ADD_CATEGORY_ID");
        } else if (paramMap.containsKey("add_category_id")) {
            productCategoryId = (String) paramMap.remove("add_category_id");
        }
        if (productCategoryId != null && productCategoryId.length() == 0) {
            productCategoryId = null;
        }
        
        if (paramMap.containsKey("ADD_ITEM_TYPE")) {
            itemType = (String) paramMap.remove("ADD_ITEM_TYPE");
        } else if (paramMap.containsKey("add_item_type")) {
            itemType = (String) paramMap.remove("add_item_type");
        }

        if (UtilValidate.isEmpty(productId)) {
            // before returning error; check make sure we aren't adding a special item type
            if (UtilValidate.isEmpty(itemType)) {
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource, "cart.addToCart.noProductInfoPassed", locale));
                return "success"; // not critical return to same page
            }
        } else {
            try {
                String pId = ProductWorker.findProductId(delegator, productId);
                if (pId != null) {
                    productId = pId;
                }
            } catch (Throwable e) {
                Debug.logWarning(e, module);
            }
        }

        // check for an itemDescription
        if (paramMap.containsKey("ADD_ITEM_DESCRIPTION")) {
            itemDescription = (String) paramMap.remove("ADD_ITEM_DESCRIPTION");
        } else if (paramMap.containsKey("add_item_description")) {
            itemDescription = (String) paramMap.remove("add_item_description");
        }
        if (itemDescription != null && itemDescription.length() == 0) {
            itemDescription = null;
        }

        // Get the ProductConfigWrapper (it's not null only for configurable items)
        ProductConfigWrapper configWrapper = null;
        configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, cart.getCurrency(), request);

        if (configWrapper != null) {
            // The choices selected by the user are taken from request and set in the wrapper
            ProductConfigWorker.fillProductConfigWrapper(configWrapper, request);
            if (!configWrapper.isCompleted()) {
                // The configuration is not valid
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource, "cart.addToCart.productConfigurationIsNotValid", locale));
                return "error";
            }
        }

        // get the override price
        if (paramMap.containsKey("PRICE")) {
            priceStr = (String) paramMap.remove("PRICE");
        } else if (paramMap.containsKey("price")) {
            priceStr = (String) paramMap.remove("price");
        }
        if (priceStr == null) {
            priceStr = "0";  // default price is 0
        }

        // get the renting data
        if (paramMap.containsKey("reservStart")) {
            reservStartStr = (String) paramMap.remove("reservStart");
            if (reservStartStr.length() == 10) // only date provided, no time string?
                    reservStartStr += " 00:00:00.000000000"; // should have format: yyyy-mm-dd hh:mm:ss.fffffffff
            if (reservStartStr.length() >0) {
                try {
                    reservStart = java.sql.Timestamp.valueOf(reservStartStr);
                } catch (Exception e) {
                    Debug.logWarning(e,"Problems parsing Reservation start string: "
                                + reservStartStr, module);
                    reservStart = null;
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource,"cart.addToCart.rental.startDate", locale));
                    return "error";
                }
            }
            else reservStart = null;

            if (paramMap.containsKey("reservEnd")) {
                reservEndStr = (String) paramMap.remove("reservEnd");
                if (reservEndStr.length() == 10) // only date provided, no time string?
                        reservEndStr += " 00:00:00.000000000"; // should have format: yyyy-mm-dd hh:mm:ss.fffffffff
                if (reservEndStr.length() > 0) {
                    try {
                        reservEnd = java.sql.Timestamp.valueOf(reservEndStr);
                    } catch (Exception e) {
                        Debug.logWarning(e,"Problems parsing Reservation end string: " + reservEndStr, module);
                        reservEnd = null;
                        request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource,"cart.addToCart.rental.endDate", locale));
                        return "error";
                    }
                }
                else reservEnd = null;
            }

            if (reservStart != null && reservEnd != null)	{
            	reservLength = new Double(UtilDateTime.getInterval(reservStart,reservEnd)/86400000);
            }



            if (reservStart != null && paramMap.containsKey("reservLength")) {
                reservLengthStr = (String) paramMap.remove("reservLength");
                // parse the reservation Length
                try {
                    reservLength = new Double(nf.parse(reservLengthStr).doubleValue());
                } catch (Exception e) {
                    Debug.logWarning(e,"Problems parsing reservation length string: "
                                    + reservLengthStr, module);
                    reservLength = new Double(1);
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderReservationLengthShouldBeAPositiveNumber", locale));
                    return "error";
                }
            }

            if (reservStart != null && paramMap.containsKey("reservPersons")) {
                reservPersonsStr = (String) paramMap.remove("reservPersons");
                // parse the number of persons
                try {
                    reservPersons = new Double(nf.parse(reservPersonsStr).doubleValue());
                } catch (Exception e) {
                    Debug.logWarning(e,"Problems parsing reservation number of persons string: " + reservPersonsStr, module);
                    reservPersons = new Double(1);
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderNumberOfPersonsShouldBeOneOrLarger", locale));
                    return "error";
                }
            }
        }

        // get the quantity
        //Begin Neogia Specific : add uom price and quantity management
        /*
        if (paramMap.containsKey("QUANTITY")) {
            quantityStr = (String) paramMap.remove("QUANTITY");
        } else if (paramMap.containsKey("quantity")) {
            quantityStr = (String) paramMap.remove("quantity");
        }
        if (UtilValidate.isEmpty(quantityStr)) {
            quantityStr = "1";  // default quantity is 1
        }
        */
        if (paramMap.containsKey("QUANTITY")) {
            uomQuantityStr = (String) paramMap.remove("QUANTITY");
        // quantity lowercase context - rental item quantity were always 1
        } else if (paramMap.containsKey("quantity")) {
            uomQuantityStr = (String) paramMap.remove("quantity");
        } else if (paramMap.containsKey("uomQuantity")) {
            uomQuantityStr = (String) paramMap.remove("uomQuantity");
        }
        if (UtilValidate.isEmpty(uomQuantityStr)) {
            uomQuantityStr = "1";  // default quantity is 1
        }
        // End neogia specific :add uom price and quantity management

        // parse the price
        try {
            price = new Double(nf.parse(priceStr).doubleValue());
        } catch (Exception e) {
            Debug.logWarning(e, "Problems parsing price string: " + priceStr, module);
            price = null;
        }

		//Begin Neogia Specific : add uom price and quantity management
        // remove an empty quantityUomId
        String quantityUomId = (String) paramMap.get("itemQuantityUomId");
        if (UtilValidate.isEmpty(quantityUomId)) {
            //add default value
            Product product = ProductServices.findByPrimaryKey(delegator,productId);
            quantityUomId = product.getStockUomId();
            paramMap.put("itemQuantityUomId", quantityUomId);
        }       
        //End Neogia SPecific : add uom price and quantity management
        // parse the quantity
        try {
            // Begin neogia specific : add uom price and quantity management
            /*
            quantity = nf.parse(quantityStr).doubleValue();
            */
            uomQuantity = nf.parse(uomQuantityStr).doubleValue();
            // End neogia specific : add uom price and quantity management
        } catch (Exception e) {
            Debug.logWarning(e, "Problems parsing quantity string: " + quantityStr, module);
            quantity = 1;
        }

		//Begin Neogia Specific : add uom price and quantity management
        //get quantity corresponding to the default uom
        if(uomQuantity>0) {
            quantity=uomQuantity;
            //if we are in variable weight we don't do conversion
            if(paramMap.get("itemQuantityUomId")!=null && paramMap.get("invoicingQtyUomId")==null)
            {
                String uomId=paramMap.get("itemQuantityUomId").toString();
                String uomIdTo=null;
                if(productId!=null) {
                    GenericValue product=null;
                    try {
                        product = (GenericValue) delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId", productId));
                    } catch (GenericEntityException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if(uomId!=null && product!=null) {
                        //check a product must note have an quantityUom if he hasn't a default Uom
                         if (product.get("stockUomId")==null)
                            //End Neogia Specific : add Uom and price management
                        {
                            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderErrorCannotHaveUomWithoutDefaultUom", locale));
                            return "error";
                        }
                        try {
                             Map requestUom=UtilMisc.toMap("uomIdFrom",uomId);
                            requestUom.put("productId", product.get("productId"));
                            requestUom.put("originalValue",new Double(uomQuantity));                   
                            if (Debug.verboseOn()) Debug.logVerbose("originalValue :"+ uomQuantity, module);
                            Map resultUom = dispatcher.runSync("conversionToProductUom",requestUom);
                            if (ServiceUtil.isError(resultUom))
                            {
                                Map msgEr=UtilMisc.toMap("uomId",uomId);
                                msgEr.put("uomIdTo",uomIdTo);
                                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderErrorCouldNotFindConversion", msgEr, locale));
                                return "error";
                            }
                            if(resultUom.get("convertedValue")!=null) {
                                quantity = ((Double)resultUom.get("convertedValue")).doubleValue();
                            }
                        }
                        catch (GenericServiceException e2) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                            Debug.logWarning(e2, "Problems accessing to services ", module);
                        } 
                    }
                }
            }
        }
		//End Neogia Specific : add uom price and quantity management
        // get the selected amount
        String selectedAmountStr = "0.00";
        if (paramMap.containsKey("ADD_AMOUNT")) {
            selectedAmountStr = (String) paramMap.remove("ADD_AMOUNT");
        } else if (paramMap.containsKey("add_amount")) {
            selectedAmountStr = (String) paramMap.remove("add_amount");
        }

        // parse the amount
        Double amount = null;
        if (selectedAmountStr != null && selectedAmountStr.length() > 0) {
            try {
                amount = new Double(nf.parse(selectedAmountStr).doubleValue());
            } catch (Exception e) {
                Debug.logWarning(e, "Problem parsing amount string: " + selectedAmountStr, module);
                amount = null;
            }
        }

        // get the ship before date (handles both yyyy-mm-dd input and full timestamp)
        shipBeforeDateStr = (String) paramMap.remove("shipBeforeDate");
        if (shipBeforeDateStr != null && shipBeforeDateStr.length() > 0) {
            if (shipBeforeDateStr.length() == 10) shipBeforeDateStr += " 00:00:00.000";
            try {
                shipBeforeDate = java.sql.Timestamp.valueOf(shipBeforeDateStr);
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e, "Bad shipBeforeDate input: " + e.getMessage(), module);
                shipBeforeDate = null;
            }
        }

        // get the ship after date (handles both yyyy-mm-dd input and full timestamp)
        shipAfterDateStr = (String) paramMap.remove("shipAfterDate");
        if (shipAfterDateStr != null && shipAfterDateStr.length() > 0) {
            if (shipAfterDateStr.length() == 10) shipAfterDateStr += " 00:00:00.000";
            try {
                shipAfterDate = java.sql.Timestamp.valueOf(shipAfterDateStr);
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e, "Bad shipAfterDate input: " + e.getMessage(), module);
                shipAfterDate = null;
            }
        }

        // check for an add-to cart survey
        List surveyResponses = null;
        if (productId != null) {
            String productStoreId = ProductStoreWorker.getProductStoreId(request);
            List productSurvey = ProductStoreWorker.getProductSurveys(delegator, productStoreId, productId, "CART_ADD", parentProductId);
            if (productSurvey != null && productSurvey.size() > 0) {
                // TODO: implement multiple survey per product
                GenericValue survey = EntityUtil.getFirst(productSurvey);
                String surveyResponseId = (String) request.getAttribute("surveyResponseId");
                if (surveyResponseId != null) {
                    surveyResponses = UtilMisc.toList(surveyResponseId);
                } else {
                    Map surveyContext = UtilHttp.getParameterMap(request);
                    GenericValue userLogin = cart.getUserLogin();
                    String partyId = null;
                    if (userLogin != null) {
                        partyId = userLogin.getString("partyId");
                    }
                    String formAction = "/additemsurvey";
                    String nextPage = RequestHandler.getNextPageUri(request.getPathInfo());
                    if (nextPage != null) {
                        formAction = formAction + "/" + nextPage;
                    }
                    ProductStoreSurveyWrapper wrapper = new ProductStoreSurveyWrapper(survey, partyId, surveyContext);
                    request.setAttribute("surveyWrapper", wrapper);
                    request.setAttribute("surveyAction", formAction); // will be used as the form action of the survey
                    return "survey";
                }
            }
        }
        if (surveyResponses != null) {
            paramMap.put("surveyResponses", surveyResponses);
        }

        // Translate the parameters and add to the cart
        result = cartHelper.addToCart(catalogId, shoppingListId, shoppingListItemSeqId, productId, productCategoryId,
                itemType, itemDescription, price, amount, quantity, reservStart, reservLength, reservPersons, 
                shipBeforeDate, shipAfterDate, configWrapper, itemGroupNumber, paramMap, parentProductId);
        controlDirective = processResult(result, request);

        // Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            if (cart.viewCartOnAdd()) {
                return "viewcart";
            } else {
                return "success";
            }
        }
    }

    public static String addToCartFromOrder(HttpServletRequest request, HttpServletResponse response) {
        String orderId = request.getParameter("orderId");
        String itemGroupNumber = request.getParameter("itemGroupNumber");
        String[] itemIds = request.getParameterValues("item_id");
        // not used yet: Locale locale = UtilHttp.getLocale(request);

        ShoppingCart cart = getCartObject(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        Map result;
        String controlDirective;

        boolean addAll = ("true".equals(request.getParameter("add_all")));
        result = cartHelper.addToCartFromOrder(catalogId, orderId, itemIds, addAll, itemGroupNumber);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    /** Adds all products in a category according to quantity request parameter
     * for each; if no parameter for a certain product in the category, or if
     * quantity is 0, do not add
     */
    public static String addToCartBulk(HttpServletRequest request, HttpServletResponse response) {
        String categoryId = request.getParameter("category_id");
        ShoppingCart cart = getCartObject(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective;
        Map result;
        // not used yet: Locale locale = UtilHttp.getLocale(request);

        //Convert the params to a map to pass in
        Map paramMap = UtilHttp.getParameterMap(request);
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        result = cartHelper.addToCartBulk(catalogId, categoryId, paramMap);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    public static String quickInitPurchaseOrder(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        
        ShoppingCart cart = new WebShoppingCart(request);
        // TODO: the code below here needs some cleanups
        cart.setBillToCustomerPartyId(request.getParameter("billToCustomerPartyId_o_0"));
        cart.setBillFromVendorPartyId(request.getParameter("supplierPartyId_o_0"));
        cart.setOrderPartyId(request.getParameter("supplierPartyId_o_0"));

        cart.setOrderType("PURCHASE_ORDER");
        
        session.setAttribute("shoppingCart", cart);
        session.setAttribute("productStoreId", cart.getProductStoreId());
        session.setAttribute("orderMode", cart.getOrderType());
        session.setAttribute("orderPartyId", cart.getOrderPartyId());

        return "success";
    }

    public static String quickCheckoutOrderWithDefaultOptions(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);

        // Set the cart's default checkout options for a quick checkout
        cart.setDefaultCheckoutOptions(dispatcher);

        return "success";
    }

    /** Adds a set of requirements to the cart
     */
    public static String addToCartBulkRequirements(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective;
        Map result;
        // not used yet: Locale locale = UtilHttp.getLocale(request);

        //Convert the params to a map to pass in
        Map paramMap = UtilHttp.getParameterMap(request);
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        result = cartHelper.addToCartBulkRequirements(catalogId, paramMap);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    /** Adds all products in a category according to default quantity on ProductCategoryMember
     * for each; if no default for a certain product in the category, or if
     * quantity is 0, do not add
     */
    public static String addCategoryDefaults(HttpServletRequest request, HttpServletResponse response) {
        String itemGroupNumber = request.getParameter("itemGroupNumber");
        String categoryId = request.getParameter("category_id");
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        ShoppingCart cart = getCartObject(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective;
        Map result;
        Double totalQuantity;
        Locale locale = UtilHttp.getLocale(request);

        result = cartHelper.addCategoryDefaults(catalogId, categoryId, itemGroupNumber);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            totalQuantity = (Double)result.get("totalQuantity");
            Map messageMap = UtilMisc.toMap("totalQuantity", UtilFormatOut.formatQuantity(totalQuantity) );

            request.setAttribute("_EVENT_MESSAGE_",
                                  UtilProperties.getMessage(resource, "cart.add_category_defaults",
                                          messageMap, locale ));

            return "success";
        }
    }

    /** Delete an item from the shopping cart. */
    public static String deleteFromCart(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(null, dispatcher, cart);
        String controlDirective;
        Map result;
        Map paramMap = UtilHttp.getParameterMap(request);
        // not used yet: Locale locale = UtilHttp.getLocale(request);

        //Delegate the cart helper
        result = cartHelper.deleteFromCart(paramMap);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    /** Update the items in the shopping cart. */
    public static String modifyCart(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ShoppingCart cart = getCartObject(request);
        Locale locale = UtilHttp.getLocale(request);
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Security security = (Security) request.getAttribute("security");
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(null, dispatcher, cart);
        String controlDirective;
        Map result;
        // not used yet: Locale locale = UtilHttp.getLocale(request);

        Map paramMap = UtilHttp.getParameterMap(request);

        String removeSelectedFlag = request.getParameter("removeSelected");
        String selectedItems[] = request.getParameterValues("selectedItem");
        boolean removeSelected = ("true".equals(removeSelectedFlag) && selectedItems != null && selectedItems.length > 0);
        result = cartHelper.modifyCart(security, userLogin, paramMap, removeSelected, selectedItems, locale);
        controlDirective = processResult(result, request);

        //Determine where to send the browser
        if (controlDirective.equals(ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    /** Empty the shopping cart. */
    public static String clearCart(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        cart.clear();

        // if this was an anonymous checkout process, go ahead and clear the session and such now that the order is placed; we don't want this to mess up additional orders and such
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        if (userLogin != null && "anonymous".equals(userLogin.get("userLoginId"))) {
            // here we want to do a full logout, but not using the normal logout stuff because it saves things in the UserLogin record that we don't want changed for the anonymous user
            session.invalidate();
            session = request.getSession(true);
            
            // to allow the display of the order confirmation page put the userLogin in the request, but leave it out of the session
            request.setAttribute("temporaryAnonymousUserLogin", userLogin);
            
            Debug.logInfo("Doing clearCart for anonymous user, so logging out but put anonymous userLogin in temporaryAnonymousUserLogin request attribute", module);
        }
        
        return "success";
    }

    /** Totally wipe out the cart, removes all stored info. */
    public static String destroyCart(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        clearCart(request, response);
        session.removeAttribute("shoppingCart");
        session.removeAttribute("orderPartyId");
        session.removeAttribute("orderMode");
        session.removeAttribute("productStoreId");
        session.removeAttribute("CURRENT_CATALOG_ID");
        return "success";
    }

    /** Gets or creates the shopping cart object */
    public static ShoppingCart getCartObject(HttpServletRequest request, Locale locale, String currencyUom) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = (ShoppingCart) request.getAttribute("shoppingCart");
        HttpSession session = request.getSession(true);
        if (cart == null) {
            cart = (ShoppingCart) session.getAttribute("shoppingCart");
        } else {
            session.setAttribute("shoppingCart", cart);
        }

        if (cart == null) {
            cart = new WebShoppingCart(request, locale, currencyUom);
            session.setAttribute("shoppingCart", cart);
        } else {
            if (locale != null && !locale.equals(cart.getLocale())) {
                cart.setLocale(locale);
            }
            if (currencyUom != null && !currencyUom.equals(cart.getCurrency())) {
                try {
                    cart.setCurrency(dispatcher, currencyUom);
                } catch (CartItemModifyException e) {
                    Debug.logError(e, "Unable to modify currency in cart", module);
                }
            }
        }
        return cart;
    }

    /** Main get cart method; uses the locale & currency from the session */
    public static ShoppingCart getCartObject(HttpServletRequest request) {
        return getCartObject(request, null, null);
    }

    /** Update the cart's UserLogin object if it isn't already set. */
    public static String keepCartUpdated(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        ShoppingCart cart = getCartObject(request);

        // if we just logged in set the UL
        if (cart.getUserLogin() == null) {
            GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
            if (userLogin != null) {
                try {
                    cart.setUserLogin(userLogin, dispatcher);
                } catch (CartItemModifyException e) {
                    Debug.logWarning(e, module);
                }
            }
        }

        // same for autoUserLogin
        if (cart.getAutoUserLogin() == null) {
            GenericValue autoUserLogin = (GenericValue) session.getAttribute("autoUserLogin");
            if (autoUserLogin != null) {
                if (cart.getUserLogin() == null) {
                    try {
                        cart.setAutoUserLogin(autoUserLogin, dispatcher);
                    } catch (CartItemModifyException e) {
                        Debug.logWarning(e, module);
                    }
                } else {
                    cart.setAutoUserLogin(autoUserLogin);
                }
            }
        }

        // update the locale
        Locale locale = UtilHttp.getLocale(request);
        if (cart.getLocale() == null || !locale.equals(cart.getLocale())) {
            cart.setLocale(locale);
        }

        return "success";
    }

    /** For GWP Promotions with multiple alternatives, selects an alternative to the current GWP */
    public static String setDesiredAlternateGwpProductId(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String alternateGwpProductId = request.getParameter("alternateGwpProductId");
        String alternateGwpLineStr = request.getParameter("alternateGwpLine");
        Locale locale = UtilHttp.getLocale(request);

        if (UtilValidate.isEmpty(alternateGwpProductId)) {
        	request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderCouldNotSelectAlternateGiftNoAlternateGwpProductIdPassed", locale));
            return "error";
        }
        if (UtilValidate.isEmpty(alternateGwpLineStr)) {
        	request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderCouldNotSelectAlternateGiftNoAlternateGwpLinePassed", locale));
            return "error";
        }

        int alternateGwpLine = 0;
        try {
            alternateGwpLine = Integer.parseInt(alternateGwpLineStr);
        } catch (Exception e) {
        	request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderCouldNotSelectAlternateGiftAlternateGwpLineIsNotAValidNumber", locale));
            return "error";
        }

        ShoppingCartItem cartLine = cart.findCartItem(alternateGwpLine);
        if (cartLine == null) {
        	request.setAttribute("_ERROR_MESSAGE_", "Could not select alternate gift, no cart line item found for #" + alternateGwpLine + ".");
            return "error";
        }

        if (cartLine.getIsPromo()) {
            // note that there should just be one promo adjustment, the reversal of the GWP, so use that to get the promo action key
            Iterator checkOrderAdjustments = UtilMisc.toIterator(cartLine.getAdjustments());
            while (checkOrderAdjustments != null && checkOrderAdjustments.hasNext()) {
                GenericValue checkOrderAdjustment = (GenericValue) checkOrderAdjustments.next();
                if (UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoId")) &&
                        UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoRuleId")) &&
                        UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoActionSeqId"))) {
                    GenericPK productPromoActionPk = delegator.makeValidValue("ProductPromoAction", checkOrderAdjustment).getPrimaryKey();
                    cart.setDesiredAlternateGiftByAction(productPromoActionPk, alternateGwpProductId);
                    if (cart.getOrderType().equals("SALES_ORDER")) {
                        org.ofbiz.order.shoppingcart.product.ProductPromoWorker.doPromotions(cart, dispatcher);
                    }
                    return "success";
                }
            }
        }

        request.setAttribute("_ERROR_MESSAGE_", "Could not select alternate gift, cart line item found for #" + alternateGwpLine + " does not appear to be a valid promotional gift.");
        return "error";
    }

    /** Associates a party to order */
    public static String addAdditionalParty(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        String partyId = request.getParameter("additionalPartyId");
        String roleTypeId[] = request.getParameterValues("additionalRoleTypeId");
        List eventList = new LinkedList();
        Locale locale = UtilHttp.getLocale(request);
        int i;

        if (UtilValidate.isEmpty(partyId) || roleTypeId.length < 1) {
        	request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderPartyIdAndOrRoleTypeIdNotDefined", locale));
            return "error";
        }

        if (request.getAttribute("_EVENT_MESSAGE_LIST_") != null) {
            eventList.addAll((List) request.getAttribute("_EVENT_MESSAGE_LIST_"));
        }

        for (i = 0; i < roleTypeId.length; i++) {
            try {
                cart.addAdditionalPartyRole(partyId, roleTypeId[i]);
            } catch (Exception e) {
                eventList.add(e.getLocalizedMessage());
            }
        }

        request.removeAttribute("_EVENT_MESSAGE_LIST_");
        request.setAttribute("_EVENT_MESSAGE_LIST_", eventList);
        return "success";
    }

    /** Removes a previously associated party to order */
    public static String removeAdditionalParty(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        String partyId = request.getParameter("additionalPartyId");
        String roleTypeId[] = request.getParameterValues("additionalRoleTypeId");
        List eventList = new LinkedList();
        Locale locale = UtilHttp.getLocale(request);
        int i;

        if (UtilValidate.isEmpty(partyId) || roleTypeId.length < 1) {
        	request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderPartyIdAndOrRoleTypeIdNotDefined", locale));
            return "error";
        }

        if (request.getAttribute("_EVENT_MESSAGE_LIST_") != null) {
            eventList.addAll((List) request.getAttribute("_EVENT_MESSAGE_LIST_"));
        }

        for (i = 0; i < roleTypeId.length; i++) {
            try {
                cart.removeAdditionalPartyRole(partyId, roleTypeId[i]);
            } catch (Exception e) {
                Debug.logInfo(e.getLocalizedMessage(), module);
                eventList.add(e.getLocalizedMessage());
            }
        }

        request.removeAttribute("_EVENT_MESSAGE_LIST_");
        request.setAttribute("_EVENT_MESSAGE_LIST_", eventList);
        return "success";
    }

    /**
     * This should be called to translate the error messages of the
     * <code>ShoppingCartHelper</code> to an appropriately formatted
     * <code>String</code> in the request object and indicate whether
     * the result was an error or not and whether the errors were
     * critical or not
     *
     * @param result    The result returned from the
     * <code>ShoppingCartHelper</code>
     * @param request The servlet request instance to set the error messages
     * in
     * @return one of NON_CRITICAL_ERROR, ERROR or NO_ERROR.
     */
    private static String processResult(Map result, HttpServletRequest request) {
        //Check for errors
        StringBuffer errMsg = new StringBuffer();
        if (result.containsKey(ModelService.ERROR_MESSAGE_LIST)) {
            List errorMsgs = (List)result.get(ModelService.ERROR_MESSAGE_LIST);
            Iterator iterator = errorMsgs.iterator();
            errMsg.append("<ul>");
            while (iterator.hasNext()) {
                errMsg.append("<li>");
                errMsg.append(iterator.next());
                errMsg.append("</li>");
            }
            errMsg.append("</ul>");
        } else if (result.containsKey(ModelService.ERROR_MESSAGE)) {
            errMsg.append(result.get(ModelService.ERROR_MESSAGE));
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
        }

        //See whether there was an error
        if (errMsg.length() > 0) {
            request.setAttribute("_ERROR_MESSAGE_", errMsg.toString());
            if (result.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                return NON_CRITICAL_ERROR;
            } else {
                return ERROR;
            }
        } else {
            return NO_ERROR;
        }
    }

    /** Assign agreement **/
    public static String selectAgreement(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String agreementId = request.getParameter("agreementId");
        Map result = cartHelper.selectAgreement(agreementId);
        if (ServiceUtil.isError(result)) {
           request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
           return "error";
        }
        return "success";
    }

    /** Assign currency **/
    public static String setCurrency(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String currencyUomId = request.getParameter("currencyUomId");
        Map result = cartHelper.setCurrency(currencyUomId);
        if (ServiceUtil.isError(result)) {
           request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
           return "error";
        }
        return "success";
    }
    
    /**
     * set the order name of the cart based on request.  right now will always return "success"
     *
     */
    public static String setOrderName(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        String orderName = request.getParameter("orderName");
        cart.setOrderName(orderName);
        return "success";
    }

    /**
     * set the PO number of the cart based on request.  right now will always return "success"
     *
     */
    public static String setPoNumber(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = getCartObject(request);
        String correspondingPoId = request.getParameter("correspondingPoId");
        cart.setPoNumber(correspondingPoId);
        return "success";
    }

    /** Add order term **/
    public static String addOrderTerm(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String termTypeId = request.getParameter("termTypeId");
        String termValue = request.getParameter("termValue");
        String termDays = request.getParameter("termDays");
        String termIndex = request.getParameter("termIndex");
        String description = request.getParameter("description");
        Locale locale = UtilHttp.getLocale(request);

        Double dTermValue = null;
        Long lTermDays = null;

        if (termValue.trim().equals("")) {
            termValue = null;
        }
        if (termDays.trim().equals("")) {
            termDays = null;
        }
        if (UtilValidate.isEmpty(termTypeId)) {
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderOrderTermTypeIsRequired", locale));
            return "error";
        }
        if (!UtilValidate.isSignedDouble(termValue)) {
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderOrderTermValue", UtilMisc.toMap("orderTermValue",UtilValidate.isSignedFloatMsg), locale));
            return "error";
        }
        if (termValue != null) {
            dTermValue =new Double(termValue);
        }
        if (!UtilValidate.isInteger(termDays)) {
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderOrderTermDays", UtilMisc.toMap("orderTermDays",UtilValidate.isLongMsg), locale));
            return "error";
        }
        if (termDays != null) {
            lTermDays = new Long(termDays);
        }
        if ((termIndex != null) && (!"-1".equals(termIndex)) && (UtilValidate.isInteger(termIndex))) {
            cartHelper.removeOrderTerm(Integer.parseInt(termIndex));
        }

        Map result = cartHelper.addOrderTerm(termTypeId, dTermValue, lTermDays, description);
        if (ServiceUtil.isError(result)) {
            request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
            return "error";
        }
        return "success";
    }

   /** Add order term **/
   public static String removeOrderTerm(HttpServletRequest request, HttpServletResponse response) {
       GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
       LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
       ShoppingCart cart = getCartObject(request);
       ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
       String index = request.getParameter("termIndex");
       Map result = cartHelper.removeOrderTerm(Integer.parseInt(index));
       if (ServiceUtil.isError(result)) {
           request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
           return "error";
       }
       return "success";
   }

    /** Initialize order entry from a shopping list **/
    public static String loadCartFromShoppingList(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");

        String shoppingListId = request.getParameter("shoppingListId");

        ShoppingCart cart = null;
        try {
            Map outMap = dispatcher.runSync("loadCartFromShoppingList",
                    UtilMisc.toMap("shoppingListId", shoppingListId,
                    "userLogin", userLogin));
            cart = (ShoppingCart)outMap.get("shoppingCart");
        } catch(GenericServiceException exc) {
            request.setAttribute("_ERROR_MESSAGE_", exc.getMessage());
            return "error";
        }
        // Begin Neogia Specific: fix a productStoreId null value error when trying to create an order from shoppingList
        String productStoreId = cart.getProductStoreId();
        if (productStoreId == null || productStoreId.length() < 1) {
        	productStoreId = UtilProperties.getPropertyValue("ProductStoreParameters.properties", "default.ProductSore.productStoreId");
            Debug.logWarning("ProductStoreId is null. Using default value from getPropertyValue : " + productStoreId, module);
        }
        // End Neogia Specific: fix a productStoreId null value error.

        session.setAttribute("shoppingCart", cart);
        // Begin Neogia Specific: using a checked productStoreId to avoid productStoreId null value
        /*
        session.setAttribute("productStoreId", cart.getProductStoreId());
        */
        session.setAttribute("productStoreId", productStoreId);
        // End Neogia Specific: using a checked productStoreId to avoid null value
        session.setAttribute("orderMode", cart.getOrderType());
        session.setAttribute("orderPartyId", cart.getOrderPartyId());

        return "success";
    }

    /** Initialize order entry from a quote **/
    public static String loadCartFromQuote(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");

        String quoteId = request.getParameter("quoteId");

        ShoppingCart cart = null;
        try {
            Map outMap = dispatcher.runSync("loadCartFromQuote",
                    UtilMisc.toMap("quoteId", quoteId,
                            "applyQuoteAdjustments", "true",
                            "userLogin", userLogin));
            cart = (ShoppingCart) outMap.get("shoppingCart");
        } catch (GenericServiceException exc) {
            request.setAttribute("_ERROR_MESSAGE_", exc.getMessage());
            return "error";
        }

        // Set the cart's default checkout options for a quick checkout
        cart.setDefaultCheckoutOptions(dispatcher);
        // Make the cart read-only
        cart.setReadOnlyCart(true);

        session.setAttribute("shoppingCart", cart);
        session.setAttribute("productStoreId", cart.getProductStoreId());
        session.setAttribute("orderMode", cart.getOrderType());
        session.setAttribute("orderPartyId", cart.getOrderPartyId());

        return "success";
    }

    /** Initialize order entry from an existing order **/
    public static String loadCartFromOrder(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");

        String quoteId = request.getParameter("orderId");

        ShoppingCart cart = null;
        try {
            Map outMap = dispatcher.runSync("loadCartFromOrder",
                                            UtilMisc.toMap("orderId", quoteId,
                                                           "userLogin", userLogin));
            cart = (ShoppingCart) outMap.get("shoppingCart");
        } catch (GenericServiceException exc) {
            request.setAttribute("_ERROR_MESSAGE_", exc.getMessage());
            return "error";
        }

        cart.setAttribute("addpty", "Y");
        session.setAttribute("shoppingCart", cart);
        session.setAttribute("productStoreId", cart.getProductStoreId());
        session.setAttribute("orderMode", cart.getOrderType());
        session.setAttribute("orderPartyId", cart.getOrderPartyId());

        return "success";
    }

    public static String createQuoteFromCart(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String destroyCart = request.getParameter("destroyCart");

        ShoppingCart cart = getCartObject(request);
        Map result = null;
        String quoteId = null;
        try {
            result = dispatcher.runSync("createQuoteFromCart",
                    UtilMisc.toMap("cart", cart,
                            "userLogin", userLogin));
            quoteId = (String) result.get("quoteId");
        } catch (GenericServiceException exc) {
            request.setAttribute("_ERROR_MESSAGE_", exc.getMessage());
            return "error";
        }
        if (ServiceUtil.isError(result)) {
           request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
           return "error";
        }
        request.setAttribute("quoteId", quoteId);
        if (destroyCart != null && destroyCart.equals("Y")) {
            ShoppingCartEvents.destroyCart(request, response);
        }

        return "success";
    }

    public static String createCustRequestFromCart(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        String destroyCart = request.getParameter("destroyCart");

        ShoppingCart cart = getCartObject(request);
        Map result = null;
        String custRequestId = null;
        try {
            result = dispatcher.runSync("createCustRequestFromCart",
                    UtilMisc.toMap("cart", cart,
                            "userLogin", userLogin));
            custRequestId = (String) result.get("custRequestId");
        } catch (GenericServiceException exc) {
            request.setAttribute("_ERROR_MESSAGE_", exc.getMessage());
            return "error";
        }
        if (ServiceUtil.isError(result)) {
           request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
           return "error";
        }
        request.setAttribute("custRequestId", custRequestId);
        if (destroyCart != null && destroyCart.equals("Y")) {
            ShoppingCartEvents.destroyCart(request, response);
        }

        return "success";
    }

    /** Initialize order entry **/
    public static String initializeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        HttpSession session = request.getSession();
        Security security = (Security) request.getAttribute("security");
        GenericValue userLogin = (GenericValue)session.getAttribute("userLogin");
        Locale locale = UtilHttp.getLocale(request);

        String productStoreId = request.getParameter("productStoreId");

        if (UtilValidate.isNotEmpty(productStoreId)) {
            session.setAttribute("productStoreId", productStoreId);
        }
        ShoppingCart cart = getCartObject(request);

        // TODO: re-factor and move this inside the ShoppingCart constructor
        String orderMode = request.getParameter("orderMode");
        if (orderMode != null) {
            cart.setOrderType(orderMode);
            session.setAttribute("orderMode", orderMode);
        } else {
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderPleaseSelectEitherSaleOrPurchaseOrder", locale));
            return "error";
        }

        // check the selected product store
        GenericValue productStore = null;
        if (UtilValidate.isNotEmpty(productStoreId)) {
            productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
            if (productStore != null) {

                // check permission for taking the order
                boolean hasPermission = false;
                if ((cart.getOrderType().equals("PURCHASE_ORDER")) && (security.hasEntityPermission("ORDERMGR", "_PURCHASE_CREATE", session))) {
                    hasPermission = true;
                } else if (cart.getOrderType().equals("SALES_ORDER")) {
                    if (security.hasEntityPermission("ORDERMGR", "_SALES_CREATE", session)) {
                        hasPermission = true;
                    } else {
                        // if the user is a rep of the store, then he also has permission
                        List storeReps = null;
                        try {
                            storeReps = delegator.findByAnd("ProductStoreRole", UtilMisc.toMap("productStoreId", productStore.getString("productStoreId"),
                                                            "partyId", userLogin.getString("partyId"), "roleTypeId", "SALES_REP"));
                        } catch(GenericEntityException gee) {
                            //
                        }
                        storeReps = EntityUtil.filterByDate(storeReps);
                        if (storeReps != null && storeReps.size() > 0) {
                            hasPermission = true;
                        }
                    }
                }

                if (hasPermission) {
                    cart = ShoppingCartEvents.getCartObject(request, null, productStore.getString("defaultCurrencyUomId"));
                } else {
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderYouDoNotHavePermissionToTakeOrdersForThisStore", locale));
                    cart.clear();
                    session.removeAttribute("orderMode");
                    return "error";
                }
                cart.setProductStoreId(productStoreId);
            } else {
                cart.setProductStoreId(null);
            }
        }

        if ("SALES_ORDER".equals(cart.getOrderType()) && UtilValidate.isEmpty(cart.getProductStoreId())) {
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderAProductStoreMustBeSelectedForASalesOrder", locale));
            cart.clear();
            session.removeAttribute("orderMode");
            return "error";
        }

        String salesChannelEnumId = request.getParameter("salesChannelEnumId");
        if (UtilValidate.isNotEmpty(salesChannelEnumId)) {
            cart.setChannelType(salesChannelEnumId);
        }

        // set party info
        // Begin neogia specific : centralized screen to order entry (JIRA998)
        /*
        String partyId = request.getParameter("supplierPartyId");
        */
        String partyId = (String)request.getAttribute("supplierPartyId"); // FIXME : getParameter or session instead get Attribute ?
        if (partyId == null || partyId.length() == 0) {
            partyId = request.getParameter("supplierPartyId");
        }
        // End neogia specific : centralized screen to order entry (JIRA998)
        if (!UtilValidate.isEmpty(request.getParameter("partyId"))) {
            partyId = request.getParameter("partyId");
        }
        String userLoginId = request.getParameter("userLoginId");
        if (partyId != null || userLoginId != null) {
            if ((partyId == null || partyId.length() == 0) && userLoginId != null && userLoginId.length() > 0) {
                GenericValue thisUserLogin = null;
                try {
                    thisUserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
                } catch(GenericEntityException gee) {
                    //
                }
                if (thisUserLogin != null) {
                    partyId = thisUserLogin.getString("partyId");
                } else {
                    partyId = userLoginId;
                }
            }
            if (partyId != null && partyId.length() > 0) {
                GenericValue thisParty = null;
                try{
                    thisParty = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                } catch(GenericEntityException gee) {
                    //
                }
                if (thisParty == null) {
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderCouldNotLocateTheSelectedParty", locale));
                    return "error";
                } else {
                    cart.setOrderPartyId(partyId);
                }
            } else if (partyId != null && partyId.length() == 0) {
                cart.setOrderPartyId("_NA_");
                partyId = null;
            }
        } else {
            partyId = cart.getPartyId();
            if (partyId != null && partyId.equals("_NA_")) partyId = null;
        }

        //Begin Neogia Specific : FR#1347107 initialisation of DefaultItemShipmentDate
        //create default estimated delivery date
        Timestamp estimatedDeliveryDate = UtilDateTime.nowTimestamp();
        Calendar calendar = Calendar.getInstance(); 
        calendar.setTime( estimatedDeliveryDate );
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        estimatedDeliveryDate = new java.sql.Timestamp(calendar.getTimeInMillis());
        estimatedDeliveryDate.setNanos(0);
        cart.setDefaultItemShipmentDate(estimatedDeliveryDate.toString());
        //initialisation of shipper
        String shipper = null;
        if (!UtilValidate.isEmpty(request.getParameter("shipper"))) {
      	  shipper = request.getParameter("shipper");
        }      
        if (shipper != null || userLoginId != null) {
            if ((shipper == null || shipper.length() == 0) && userLoginId != null && userLoginId.length() > 0) {
                GenericValue thisUserLogin = null;
                try {
                    thisUserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
                } catch(GenericEntityException gee) {
                    //
                }
                if (thisUserLogin != null) {
              	  shipper = thisUserLogin.getString("partyId");
                } else {
              	  shipper = userLoginId;
                }
            }
            if (shipper != null && shipper.length() > 0) {
                GenericValue thisParty = null;
                try{
                    thisParty = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", shipper));
                } catch(GenericEntityException gee) {
                    //
                }
                if (thisParty == null) {
                  request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderCouldNotLocateTheSelectedParty", locale));
                  return "error";
                } else {            	  
                  cart.setShipToCustomerPartyId(shipper);
                  List list;
                  try {
                	  list = delegator.findByAnd("PartyAndPostalAddress",UtilMisc.toMap("partyId",shipper,"contactMechTypeId","POSTAL_ADDRESS"));
                	  list = EntityUtil.filterByDate(list,new Date());
                	  if(list.size()>0)
                	  {                		
                		  // only initialize shippingContactMechId with contactMech of userLogin when cart type is sales order
                		  if ("SALES_ORDER".equals(cart.getOrderType()))
                		  cart.setShippingContactMechId((String)((Map)list.get(0)).get("contactMechId"));
                	  }
            	  } catch (GenericEntityException e) {						
            		  e.printStackTrace();
            	  }
              }
          } else if (shipper != null && shipper.length() == 0) {
              cart.setShipToCustomerPartyId("_NA_");
              shipper = null;
          }
      } else {
    	  shipper = cart.getShipToCustomerPartyId();
          if (shipper != null && shipper.equals("_NA_")) shipper = null;
      }
      LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
      //now we search last for the partyId choosen
      String nbOldOrder = UtilProperties.getPropertyValue("processOrder", "default.ProcessOrder.nbOldOrder");
      
      int nb_order = 0;
      if(nbOldOrder!=null&&!"".equalsIgnoreCase(nbOldOrder))
      {
    	  Integer nb = null;
    	  try{
    		  nb = new Integer(nbOldOrder);
    	  }catch(NumberFormatException e)
    	  {
    		  nb=new Integer(0);
    	  }    	  
    	  nb_order = nb.intValue();
      }      
      
      if ("SALES_ORDER".equals(cart.getOrderType()))
      {
    	  try {
              HashMap mapItem = new HashMap();
              String catalogId = CatalogWorker.getCurrentCatalogId(request);
              if (nb_order > 0){
//               sorting by order date newest first
                List orderBy = UtilMisc.toList("-orderDate", "-orderId");
                List list = delegator.findByAnd("OrderHeaderAndRoles",UtilMisc.toMap("partyId",partyId,"roleTypeId","PLACING_CUSTOMER"),orderBy);
                Iterator ti = list.iterator();
                int nb = 0;
                  while (ti.hasNext()&&nb<nb_order) {
                      GenericValue order = (GenericValue) ti.next();
                      List listItem = delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",order.getString("orderId")));
                      Iterator items = listItem.iterator();
                      while (items.hasNext()) {
                          GenericValue item = (GenericValue) items.next();
                          if(mapItem.get(item.getString("productId"))==null)
                          {
                            mapItem.put(item.getString("productId"),item.getString("productId"));                                                           
                            try {
                                double amount = 0.00;
                                if (item.get("selectedAmount") != null) {
                                    amount = item.getDouble("selectedAmount").doubleValue();
                                }
                                int index = cart.addOrIncreaseItem(item.getString("productId"), new Double(amount), item.getDouble("quantity").doubleValue(),null, null, null, null, null, null, null, catalogId, null, null, null, null, dispatcher);                         
                                ShoppingCartItem item_insert = cart.findCartItem(index);
                                item_insert.setQuantity(0,dispatcher,cart,true,false);
                                //create default estimated delivery date
                                item_insert.setDesiredDeliveryDate(estimatedDeliveryDate);
					        // copy attribute
					        item_insert.setQuantityUomId(item.getString("quantityUomId"));
					        List listAttr = delegator.findByAnd("OrderItemAttribute",UtilMisc.toMap("orderId",order.getString("orderId"),"orderItemSeqId",item.getString("orderItemSeqId")));
					        for(Iterator iter = listAttr.iterator(); iter.hasNext();)
					        {
					        	GenericValue attrItem = (GenericValue) iter.next();
					        	String value = "0";
					        	if(!attrItem.getString("attrName").equalsIgnoreCase("quantityPack"))
					        	{
					        		value = attrItem.getString("attrValue");
					        	}
					        	item_insert.setAttribute(attrItem.getString("attrName"),value);
					        }					        
					        //put quantityInvoice in attribute
					        item_insert.setAttribute("invoicingQuantity","0");
					        String uomIdConv = (String)item.get("invoicingQtyUomId");
					        item_insert.setAttribute("invoicingQtyUomId",uomIdConv);
					        String qtyuomId = (String)item.get("quantityUomId");
					        
					        try {
								Map conv = dispatcher.runSync("getProdConvertion", UtilMisc.toMap("productId", item.getString("productId"),"uomId", qtyuomId,"uomIdTo",uomIdConv,"userLogin", userLogin,"locale",locale));
								item_insert.setAttribute("convertInvoice",((Map)conv.get("result")).get("conv").toString());
					        } catch (GenericServiceException e) {
					        	item_insert.setAttribute("convertInvoice","1");
								Debug.logError( e.getLocalizedMessage(), module);
							}
					        
					        
					        
                            } catch (CartItemModifyException e) {
								Debug.logError( e.getLocalizedMessage(), module);
                            } catch (ItemNotFoundException e) {
                                // TODO Auto-generated catch block
								Debug.logError( e.getLocalizedMessage(), module);
                            }
                          }
                      }
                      nb++;                                               
                  }
              }
//          call sales product caddy
// FR#1392006 servce to get product list of agreementProductAppl
            try {            	
            	String partyIdFrom = null;
    	        if(!UtilValidate.isEmpty(productStore)){
    	        	partyIdFrom = productStore.getString("payToPartyId");    	                    	
					Map prod_cad = dispatcher.runSync("getProductListAgreement", UtilMisc.toMap("partyIdTo", partyId, "partyIdFrom", partyIdFrom, "agreementTypeId", "SALES_AGREEMENT", "userLogin", userLogin,"locale",locale));								
					List list_prod = (List)prod_cad.get("productList");
					for(int p = 0;p<list_prod.size();p++)
					{
						//Map map_prod = (Map)list_prod.get(p);
						//GenericValue prod = (GenericValue)map_prod.get("products");
						String productId = (String)list_prod.get(p);
						if(mapItem.get(productId)==null)
	                    {
	                    	mapItem.put(productId,productId);                                      	                    
		                    try {
		                    	double amount = 0.00;
								int index = cart.addOrIncreaseItem(productId, new Double(amount), 1.0, null, null, null, null, null, null, null, catalogId, null, null, null, null, dispatcher);							
						        ShoppingCartItem item_insert = cart.findCartItem(index);
						        item_insert.setQuantity(0,dispatcher,cart,true,false);
							} catch (CartItemModifyException e) {
								Debug.logError( e.getLocalizedMessage(), module);
							} catch (ItemNotFoundException e) {
								Debug.logError( e.getLocalizedMessage(), module);
							}
	                    }					
					}				
    	        }
			} catch (GenericServiceException e) {
			   Debug.logError( e.getLocalizedMessage(), module);
			}
    	} catch (GenericEntityException e) {
    	  Debug.logError( e.getLocalizedMessage(), module);
		}	  
      }
        //ENd Neogia Specific : FR#1347107 initialisation of DefaultItemShipmentDate
        return "success";
    }

    /** Route order entry **/
    public static String routeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        // if the order mode is not set in the attributes, then order entry has not been initialized
        if (session.getAttribute("orderMode") == null) {
            return "init";
        }

        // if the request is coming from the init page, then orderMode will be in the request parameters
        if (request.getParameter("orderMode") != null) {
            return "agreements"; // next page after init is always agreements
        }

        // orderMode is set and there is an order in progress, so go straight to the cart
        return "cart";
    }

    public static String doManualPromotions(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        ShoppingCart cart = getCartObject(request);
        List manualPromotions = new LinkedList();

        // iterate through the context and find all keys that start with "productPromoId_"
        Map context = UtilHttp.getParameterMap(request);
        String keyPrefix = "productPromoId_";
        for (int i = 1; i <= 50; i++) {
            String productPromoId = (String)context.get(keyPrefix + i);
            if (UtilValidate.isNotEmpty(productPromoId)) {
                try {
                    GenericValue promo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
                    if (promo != null) {
                        manualPromotions.add(promo);
                    }
                } catch(GenericEntityException gee) {
                    request.setAttribute("_ERROR_MESSAGE_", gee.getMessage());
                    return "error";
                }
            } else {
                break;
            }
        }
        ProductPromoWorker.doPromotions(cart, manualPromotions, dispatcher);
        return "success";
    }
  
    //FR#1347107 sn new method to update order header with more parameters
    public static String updateOrderHeaderDetailed(HttpServletRequest request, HttpServletResponse response){
		String shipmentDate = request.getParameter("shipmentDate");		
		ShoppingCart shoppingCart = org.ofbiz.order.shoppingcart.ShoppingCartEvents.getCartObject(request);
		
		shoppingCart.setDefaultItemShipmentDate(shipmentDate);
		
		String shipper = request.getParameter("shipper_header");
		String buyer = request.getParameter("buyer_header");
		
		//on va chercher l'id du contact_mech
		GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
		List list;
		try {
			list = delegator.findByAnd("PartyAndPostalAddress",UtilMisc.toMap("partyId",shipper,"contactMechTypeId","POSTAL_ADDRESS"));
			if(list.size()>0)
			{
				shoppingCart.setShippingContactMechId((String)((Map)list.get(0)).get("contactMechId"));
			}
			else
				shoppingCart.setShippingContactMechId("");
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//we read last shippper
		String last_shipper = shoppingCart.getShipToCustomerPartyId();
		//remove last shipper role for order
		shoppingCart.removeAdditionalPartyRole(last_shipper, "SHIP_TO_CUSTOMER");
		//add new shipper and his role
		shoppingCart.setShipToCustomerPartyId(shipper);
		shoppingCart.addAdditionalPartyRole(shipper, "SHIP_TO_CUSTOMER");
		shoppingCart.setOrderPartyId(buyer);
		return "success";
	}
    //FR#1347107 en


    public static String bulkAddProducts(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        String controlDirective = null;
        Map result = null;
        String productId = null;
        String productCategoryId = null;
        String quantityStr = null;
        String itemDesiredDeliveryDateStr = null;
        double quantity = 0;
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        String itemType = null;
        String itemDescription = "";

        String rowCountField = null;
        int rowCount = 0;            // number of rows of products to add
        String DELIMITER = "_o_";    // delimiter, separating field from row number

        // Get the parameters as a MAP, remove the productId and quantity params.
        Map paramMap = UtilHttp.getParameterMap(request);

        String itemGroupNumber = request.getParameter("itemGroupNumber");

        // Get shoppingList info if passed.  I think there can only be one shoppingList per request
        String shoppingListId = request.getParameter("shoppingListId");
        String shoppingListItemSeqId = request.getParameter("shoppingListItemSeqId");

        // try to get the rowCount information passed in from request
        if (paramMap.containsKey("_rowCount")) {
            rowCountField = (String) paramMap.remove("_rowCount");
        } else {
            Debug.logWarning("No _rowCount was passed in", ShoppingCartEvents.module);
        }
        try {
            rowCount = Integer.parseInt(rowCountField);
        } catch (NumberFormatException e) {
            Debug.logWarning("Invalid value for rowCount =" + rowCountField, module);
        }

        if (rowCount < 1) {
            Debug.logWarning("No rows to process, as rowCount = " + rowCount, module);
        } else {
            for (int i = 0; i < rowCount; i++) {
                controlDirective = null;                // re-initialize each time
                String thisSuffix = DELIMITER + i;        // current suffix after each field id

                // get the productId
                if (paramMap.containsKey("productId" + thisSuffix)) {
                    productId = (String) paramMap.remove("productId" + thisSuffix);
                }

                if (paramMap.containsKey("quantity" + thisSuffix)) {
                    quantityStr = (String) paramMap.remove("quantity" + thisSuffix);
                }
                if ((quantityStr == null) || (quantityStr.equals(""))){    // otherwise, every empty value causes an exception and makes the log ugly
                    quantityStr = "0";  // default quantity is 0, so without a quantity input, this field will not be added
                }

                // parse the quantity
                try {
                    quantity = NumberFormat.getNumberInstance().parse(quantityStr).doubleValue();
                } catch (Exception e) {
                    Debug.logWarning(e, "Problems parsing quantity string: " + quantityStr, module);
                    quantity = 0;
                }

                // get the selected amount
                String selectedAmountStr = "0.00";
                if (paramMap.containsKey("amount" + thisSuffix)) {
                    selectedAmountStr = (String) paramMap.remove("amount" + thisSuffix);
                }

                // parse the amount
                Double amount = null;
                if (selectedAmountStr != null && selectedAmountStr.length() > 0) {
                    try {
                        amount = new Double(NumberFormat.getNumberInstance().parse(selectedAmountStr).doubleValue());
                    } catch (Exception e) {
                        Debug.logWarning(e, "Problem parsing amount string: " + selectedAmountStr, module);
                        amount = null;
                    }
                }

                if (paramMap.containsKey("itemDesiredDeliveryDate" + thisSuffix)) {
                    itemDesiredDeliveryDateStr = (String) paramMap.remove("itemDesiredDeliveryDate" + thisSuffix);
                }
                // get the item type
                if (paramMap.containsKey("itemType" + thisSuffix)){
                    itemType = (String) paramMap.remove("itemType" + thisSuffix);
                }

                if (paramMap.containsKey("itemDescription" + thisSuffix)){
                    itemDescription = (String) paramMap.remove("itemDescription" + thisSuffix);
                }

                Map itemAttributes = UtilMisc.toMap("itemDesiredDeliveryDate", itemDesiredDeliveryDateStr);

                if (quantity > 0) {
                    Debug.logInfo("Attempting to add to cart with productId = " + productId + ", categoryId = " + productCategoryId +
                            ", quantity = " + quantity + ", itemType = " + itemType + " and itemDescription = " + itemDescription, module);
                    result = cartHelper.addToCart(catalogId, shoppingListId, shoppingListItemSeqId, productId, 
                                                  productCategoryId, itemType, itemDescription, null, 
                                                  amount, quantity, null, null, null, null, null, null, 
                                                  itemGroupNumber, itemAttributes,null);
                    // no values for price and paramMap (a context for adding attributes)
                    controlDirective = processResult(result, request);
                    if (controlDirective.equals(ERROR)){    // if the add to cart failed, then get out of this loop right away
                        return "error";
                    }
                }
            }
        }

        // Determine where to send the browser
        return cart.viewCartOnAdd() ? "viewcart" : "success";
    }

    // request method for setting the currency, agreement and shipment dates at once
    public static String setOrderCurrencyAgreementShipDates(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        ShoppingCart cart = getCartObject(request);
        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);

        String agreementId = request.getParameter("agreementId");
        String currencyUomId = request.getParameter("currencyUomId");
        String shipBeforeDateStr = request.getParameter("shipBeforeDate");
        String shipAfterDateStr = request.getParameter("shipAfterDate");
        String orderName = request.getParameter("orderName");
        String correspondingPoId = request.getParameter("correspondingPoId");
        Map result = null;

        // set the agreement if specified otherwise set the currency
        if (agreementId != null && agreementId.length() > 0) {
            result = cartHelper.selectAgreement(agreementId);
        } else { 
            result = cartHelper.setCurrency(currencyUomId);
        }
        if (ServiceUtil.isError(result)) {
            request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(result));
            return "error";
        }

        // set the order name
        cart.setOrderName(orderName);
        
        // set the corresponding purchase order id
        cart.setPoNumber(correspondingPoId);
        
        // set the default ship before and after dates if supplied
        try {
            if (UtilValidate.isNotEmpty(shipBeforeDateStr)) {
                if (shipBeforeDateStr.length() == 10) shipBeforeDateStr += " 00:00:00.000";
                cart.setDefaultShipBeforeDate(java.sql.Timestamp.valueOf(shipBeforeDateStr));
            }
            if (UtilValidate.isNotEmpty(shipAfterDateStr)) {
                if (shipAfterDateStr.length() == 10) shipAfterDateStr += " 00:00:00.000";
                cart.setDefaultShipAfterDate(java.sql.Timestamp.valueOf(shipAfterDateStr));
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        }
        return "success";
    }
}
