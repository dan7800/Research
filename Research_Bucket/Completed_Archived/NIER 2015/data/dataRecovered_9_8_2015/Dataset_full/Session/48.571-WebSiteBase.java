/*
 *  Generator-Id: GeneratorEntityObjectBaseJava1.java,v 1.69 2007/01/18 22:21:59 soledad Exp 
 *  Copyright (c) 2004, 2006 Neogia - www.neogia.org
 *
 *  This file is part of OfbizNeogia.
 *  
 *  OfbizNeogia is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  OfbizNeogia is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OfbizNeogia; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  This file has been generated and will be re-generated.
 */
package org.ofbiz.content.webapp.generated;

import java.util.Map;
import java.util.List;
import java.util.Locale;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.common.ValueNullRuntimeException;


import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;


public class WebSiteBase extends org.neogia.PersistantObject {

    private static final long serialVersionUID = 1L;

    public static final String module = WebSiteBase.class.getName();
    public static final String resource = "ContentUiLabels";

    /** name of the entity used to store instances of this class in database */
    public static final String ENTITY_NAME = "WebSite";

    public String getWebSiteId() {
        return ((String) get("webSiteId"));
    }

    public String getSiteName() {
        return ((String) get("siteName"));
    }
    
    public void setSiteName(String _siteName) {
        if (_siteName != null) {
            set("siteName", _siteName);
        } else if (get("siteName") != null) {
            set("siteName", null);
        }
    }

    public String getAllowProductStoreChange() {
        return ((String) get("allowProductStoreChange"));
    }
    
    public void setAllowProductStoreChange(String _allowProductStoreChange) {
        if (_allowProductStoreChange != null) {
            set("allowProductStoreChange", _allowProductStoreChange);
        } else if (get("allowProductStoreChange") != null) {
            set("allowProductStoreChange", null);
        }
    }

    public String getHttpHost() {
        return ((String) get("httpHost"));
    }
    
    public void setHttpHost(String _httpHost) {
        if (_httpHost != null) {
            set("httpHost", _httpHost);
        } else if (get("httpHost") != null) {
            set("httpHost", null);
        }
    }

    public boolean httpPortNotEmpty() {
        return get("httpPort") != null ? true : false;
    }

    public int getHttpPort() {
        Long httpPort = (Long) get("httpPort");
        if (httpPort != null) {
            return ((Long) httpPort).intValue();
        } else {
            throw new ValueNullRuntimeException("In webSite, attribute httpPort is null");
        }
    }

    /**
     * return getHttpPort() if httpPortNotEmpty() otherwise return defaultValue.
     * @param defaultValue
     */
    public int getHttpPort(int defaultValue) {
        if (httpPortNotEmpty()) {
            return getHttpPort();
        } else {
            return defaultValue;
        }
    }

    public void setHttpPort2Null() {
        set("httpPort", null);
    }

    public void setHttpPort(int _httpPort) {
        set("httpPort", new Long(_httpPort));
    }

    public String getHttpsHost() {
        return ((String) get("httpsHost"));
    }
    
    public void setHttpsHost(String _httpsHost) {
        if (_httpsHost != null) {
            set("httpsHost", _httpsHost);
        } else if (get("httpsHost") != null) {
            set("httpsHost", null);
        }
    }

    public boolean httpsPortNotEmpty() {
        return get("httpsPort") != null ? true : false;
    }

    public int getHttpsPort() {
        Long httpsPort = (Long) get("httpsPort");
        if (httpsPort != null) {
            return ((Long) httpsPort).intValue();
        } else {
            throw new ValueNullRuntimeException("In webSite, attribute httpsPort is null");
        }
    }

    /**
     * return getHttpsPort() if httpsPortNotEmpty() otherwise return defaultValue.
     * @param defaultValue
     */
    public int getHttpsPort(int defaultValue) {
        if (httpsPortNotEmpty()) {
            return getHttpsPort();
        } else {
            return defaultValue;
        }
    }

    public void setHttpsPort2Null() {
        set("httpsPort", null);
    }

    public void setHttpsPort(int _httpsPort) {
        set("httpsPort", new Long(_httpsPort));
    }

    public String getEnableHttps() {
        return ((String) get("enableHttps"));
    }
    
    public void setEnableHttps(String _enableHttps) {
        if (_enableHttps != null) {
            set("enableHttps", _enableHttps);
        } else if (get("enableHttps") != null) {
            set("enableHttps", null);
        }
    }

    public String getStandardContentPrefix() {
        return ((String) get("standardContentPrefix"));
    }
    
    public void setStandardContentPrefix(String _standardContentPrefix) {
        if (_standardContentPrefix != null) {
            set("standardContentPrefix", _standardContentPrefix);
        } else if (get("standardContentPrefix") != null) {
            set("standardContentPrefix", null);
        }
    }

    public String getSecureContentPrefix() {
        return ((String) get("secureContentPrefix"));
    }
    
    public void setSecureContentPrefix(String _secureContentPrefix) {
        if (_secureContentPrefix != null) {
            set("secureContentPrefix", _secureContentPrefix);
        } else if (get("secureContentPrefix") != null) {
            set("secureContentPrefix", null);
        }
    }

    public String getCookieDomain() {
        return ((String) get("cookieDomain"));
    }
    
    public void setCookieDomain(String _cookieDomain) {
        if (_cookieDomain != null) {
            set("cookieDomain", _cookieDomain);
        } else if (get("cookieDomain") != null) {
            set("cookieDomain", null);
        }
    }
                              
                                 
    public List getWebSiteRoles() {
        try {
            return getRelated("WebSiteRole");
        } catch (GenericEntityException e) {
            Debug.logError("Error WebSite.getRelated WebSiteRole :" + e.getMessage(),  module);
        }
        return null;
    }
    
    public List getWebSiteRolesCache() {
        try { 
            return getRelatedCache("WebSiteRole");
        } catch (GenericEntityException e) {
            Debug.logError("Error WebSite.getRelatedCache WebSiteRole :" + e.getMessage(),  module);
        }
        return null;
    }

    /**
     *  For each attribute test if the context has a value for it and
     *    make the standard check parametered in UML diagram
     *    if it's ok, update the object with the context value
     */
    public Map checkUpdateFromContext(String action, Map context) {
        Locale locale = (Locale) context.get("locale");

        if (context.containsKey("siteName")|| action.equals("add")) {
            String siteName = (String) context.get("siteName");
            if (siteName == null && action.equals("add")) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ContentSiteNameMandatory", locale));
            }
            this.setSiteName(siteName);
        }

        if (context.containsKey("allowProductStoreChange")) {
            String allowProductStoreChange = (String) context.get("allowProductStoreChange");
            this.setAllowProductStoreChange(allowProductStoreChange);
        }

        if (context.containsKey("httpHost")) {
            String httpHost = (String) context.get("httpHost");
            this.setHttpHost(httpHost);
        }

        if (context.containsKey("httpPort")|| action.equals("add")) {
            Long httpPort = (Long) context.get("httpPort");
            if (httpPort == null && action.equals("add")) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ContentHttpPortMandatory", locale));
            }
            if (httpPort != null) {
                this.setHttpPort(httpPort.intValue());
            } else if (this.httpPortNotEmpty()) {
                this.setHttpPort2Null();
            }
        }

        if (context.containsKey("httpsHost")) {
            String httpsHost = (String) context.get("httpsHost");
            this.setHttpsHost(httpsHost);
        }

        if (context.containsKey("httpsPort")|| action.equals("add")) {
            Long httpsPort = (Long) context.get("httpsPort");
            if (httpsPort == null && action.equals("add")) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource, "ContentHttpsPortMandatory", locale));
            }
            if (httpsPort != null) {
                this.setHttpsPort(httpsPort.intValue());
            } else if (this.httpsPortNotEmpty()) {
                this.setHttpsPort2Null();
            }
        }

        if (context.containsKey("enableHttps")) {
            String enableHttps = (String) context.get("enableHttps");
            this.setEnableHttps(enableHttps);
        }

        if (context.containsKey("standardContentPrefix")) {
            String standardContentPrefix = (String) context.get("standardContentPrefix");
            this.setStandardContentPrefix(standardContentPrefix);
        }

        if (context.containsKey("secureContentPrefix")) {
            String secureContentPrefix = (String) context.get("secureContentPrefix");
            this.setSecureContentPrefix(secureContentPrefix);
        }

        if (context.containsKey("cookieDomain")) {
            String cookieDomain = (String) context.get("cookieDomain");
            this.setCookieDomain(cookieDomain);
        }

        return null;
    }

    /**
     *  Prepare a map useable for the FormWrapper, put all the attribute execpt PK and hidden attribute
     *    for association put PK and the toString method result
     *    before put it test if the parameter has a value for it and
     */
    public void toFormMap(Map formData, Locale locale) {
        if (formData.get("siteName") == null) {
            formData.put("siteName", get("siteName"));
        }
        if (formData.get("allowProductStoreChange") == null) {
            formData.put("allowProductStoreChange", get("allowProductStoreChange"));
        }
        if (formData.get("httpHost") == null) {
            formData.put("httpHost", get("httpHost"));
        }
        if (formData.get("httpPort") == null) {
            formData.put("httpPort", get("httpPort"));
        }
        if (formData.get("httpsHost") == null) {
            formData.put("httpsHost", get("httpsHost"));
        }
        if (formData.get("httpsPort") == null) {
            formData.put("httpsPort", get("httpsPort"));
        }
        if (formData.get("enableHttps") == null) {
            formData.put("enableHttps", get("enableHttps"));
        }
        if (formData.get("standardContentPrefix") == null) {
            formData.put("standardContentPrefix", get("standardContentPrefix"));
        }
        if (formData.get("secureContentPrefix") == null) {
            formData.put("secureContentPrefix", get("secureContentPrefix"));
        }
        if (formData.get("cookieDomain") == null) {
            formData.put("cookieDomain", get("cookieDomain"));
        }
    }

    /**
     * Build a map representing the object for reporting engine
     */
    public Map toReportMap(Locale locale) {

        Map record = UtilMisc.toMap(new Object[0]);
        record.put("ContentWebSite", UtilProperties.getMessage(resource, "ContentWebSite", locale));

        record.put("ContentwebSiteId", UtilProperties.getMessage(resource, "ContentWebSiteId", locale));

        record.put("webSiteId", this.getWebSiteId());

        record.put("ContentsiteName", UtilProperties.getMessage(resource, "ContentSiteName", locale));

        if (this.getSiteName() != null) {
            record.put("siteName", this.getSiteName());
        } else {
            record.put("siteName", null);
        }

        record.put("ContentwebSiteId", UtilProperties.getMessage(resource, "ContentWebSiteId", locale));

        record.put("webSiteId", this.getWebSiteId());

        record.put("ContentsiteName", UtilProperties.getMessage(resource, "ContentSiteName", locale));

        if (this.getSiteName() != null) {
            record.put("siteName", this.getSiteName());
        } else {
            record.put("siteName", null);
        }

        record.put("ContenthttpHost", UtilProperties.getMessage(resource, "ContentHttpHost", locale));

        if (this.getHttpHost() != null) {
            record.put("httpHost", this.getHttpHost());
        } else {
            record.put("httpHost", null);
        }

        record.put("ContenthttpPort", UtilProperties.getMessage(resource, "ContentHttpPort", locale));

        if (this.httpPortNotEmpty()) {
             record.put("httpPort", new Long(this.getHttpPort()));
        } else {
             record.put("httpPort", null);
        }

        record.put("ContenthttpsHost", UtilProperties.getMessage(resource, "ContentHttpsHost", locale));

        if (this.getHttpsHost() != null) {
            record.put("httpsHost", this.getHttpsHost());
        } else {
            record.put("httpsHost", null);
        }

        record.put("ContenthttpsPort", UtilProperties.getMessage(resource, "ContentHttpsPort", locale));

        if (this.httpsPortNotEmpty()) {
             record.put("httpsPort", new Long(this.getHttpsPort()));
        } else {
             record.put("httpsPort", null);
        }

        record.put("ContentenableHttps", UtilProperties.getMessage(resource, "ContentEnableHttps", locale));

        if (this.getEnableHttps() != null) {
            record.put("enableHttps", this.getEnableHttps());
        } else {
            record.put("enableHttps", null);
        }
        return record;
    }


    public Map beforeStore(Map context) {
        return null;
    }
    /**
     * @deprecated use create() or store() methods instead
     *
     * <p>
     * Create or update the object in database.
     * </p>
     *
     * @return <code>true</code> if no database error occurs
     */
     public boolean nStore() {
        try {
            getDelegator().createOrStore(this);
            return true;
        } catch (GenericEntityException e) {
            Debug.logError("create or strore in WebSite :" + e.getMessage(),  module);
        }
        return false;
    }
    public Map afterStore(Map context) {
        return null;
    }


    public Map beforeRemove(Map context) {
        return null;
    }
    /**
     * @deprecated use remove() instead
     *
     * <p>
     * Delete the object from the database.
     * </p>
     *
     * @return <code>true</code> if no database error occurs
     */
     public boolean nRemove() {
        try {
            remove();
            return true;
        } catch (GenericEntityException e) {
            Debug.logError("remove in WebSite :" + e.getMessage(),  module);
        }
        return false;
    }
    public Map afterRemove(Map context) {
        return null;
    }


    public String toDisplayString() {
        String toString ="";
        if (get("webSiteId") != null) toString = "[" + getWebSiteId() + "] ";
        return toString + getSiteName();

    }

}
