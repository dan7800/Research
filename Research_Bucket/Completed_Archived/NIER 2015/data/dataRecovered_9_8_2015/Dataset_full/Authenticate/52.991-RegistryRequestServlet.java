/*
 * UrlRedirector.java
 *
 * Created on October 1, 2003, 2:41 PM
 */

package org.freebxml.omar.client.ui.thin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.ui.common.UIUtility;
import org.freebxml.omar.client.ui.common.conf.bindings.Configuration;
import org.freebxml.omar.client.ui.common.conf.bindings.MethodParameter;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectFactory;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfig;
import org.freebxml.omar.client.ui.common.conf.bindings.ObjectTypeConfigType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsColumnType;
import org.freebxml.omar.client.ui.common.conf.bindings.SearchResultsConfigType;
import org.freebxml.omar.client.xml.registry.BulkResponseImpl;
import org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;
import org.freebxml.omar.client.xml.registry.ConnectionImpl;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.infomodel.ConceptImpl;
import org.freebxml.omar.client.xml.registry.infomodel.InternationalStringImpl;
import org.freebxml.omar.client.xml.registry.util.KeystoreUtil;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.xml.registry.util.SecurityUtil;
import org.freebxml.omar.common.RegistryResponseHolder;
import org.freebxml.omar.common.SOAPMessenger;





/**
 *
 * @author  psterk
 * @version
 */
public class RegistryRequestServlet extends HttpServlet
{
    static final Log log = 
        LogFactory.getLog(RegistryRequestServlet.class);    
    private static final String SQL_WHERE = " WHERE ";
    
    private SOAPMessenger soapMessenger = null;
    private ConnectionImpl connection = null;
    private BusinessQueryManagerImpl bqm = null;
    private BusinessLifeCycleManagerImpl blcm = null;
    private HashMap objectTypeToConfigMap = new HashMap();
    private String restUrl = null;

    //Current configuration. Changes for each search
    private ObjectTypeConfig otCfg = null;
    private SearchResultsConfigType srCfg = null;
    private List srCols = null;
    
    /* TODO note: detect if the user has a default search results 
     * configuration, i.e. - Name and Description. If true, set
     * returnComposedObjects=false to conserve resources and speed processing
     */
    private String beginMessage = "<?xml version=\"1.0\"?>"+
        "<AdhocQueryRequest "+
        "xmlns=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\" "+
        "xmlns:query=\"urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0\">"+
        "<query:ResponseOption returnType=\"LeafClassWithRepositoryItem\" "+ 
        "returnComposedObjects=\"true\"/>"+
        "<query:SQLQuery id=\"tempId\">"+
        "<query:QueryString xmlns=\"\">";
    private String endMessage = "</query:QueryString></query:SQLQuery>"+
        "</AdhocQueryRequest>";
    

    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {   
            ProviderProperties props = ProviderProperties.getInstance();
            String registryUrl = props.getProperty("jaxr-ebxml.soap.url");
            restUrl = props.getProperty("jaxr-ebxml.rest.url");
            if (restUrl == null) {
                restUrl = "http://localhost:8080/omar/registry/rest";
            }
            if (registryUrl == null) {
                registryUrl = "http://localhost:8080/omar/registry/soap";
            }            
            props.put("javax.xml.registry.queryManagerURL", registryUrl);
            System.setProperty("javax.xml.registry.ConnectionFactoryClass",
                "org.freebxml.omar.client.xml.registry.ConnectionFactoryImpl");
            ConnectionFactory connFactory = ConnectionFactory.newInstance();
            connection = (ConnectionImpl)connFactory.createConnection();
            RegistryService service = connection.getRegistryService();
            bqm = (BusinessQueryManagerImpl)service.getBusinessQueryManager();
            blcm = (BusinessLifeCycleManagerImpl)service.getBusinessLifeCycleManager();
            soapMessenger = new SOAPMessenger(registryUrl, connection.getCredentialInfo());
            loadConfiguration();
            log.info(getClass().getName()+" is initialized");
        } 
        catch (Throwable t) {
            t.printStackTrace();
            throw new ServletException(t.getMessage());
        }
    }
    
    private void loadConfiguration() throws JAXRException {
        Configuration cfg = UIUtility.getInstance().getConfiguration();

        List otCfgs = cfg.getObjectTypeConfig();
        Iterator iter = otCfgs.iterator();

        while (iter.hasNext()) {
            ObjectTypeConfigType otCfg = (ObjectTypeConfigType) iter.next();
            String id = otCfg.getId();
            objectTypeToConfigMap.put(id, otCfg);
        }
    }
        
    /** Destroys the servlet.
     */
    public void destroy()
    {
        try
        {
            connection.close();
        } 
        catch (Throwable t) {}
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code>
      * methods.
      *
      * @param request
      *     servlet request
      * @param response
      *     servlet response
      */
    protected void processRequest(HttpServletRequest request, 
                                  HttpServletResponse response)
        throws ServletException, IOException
    {
        try {
            try {
                
                response.setContentType("text/html");        
                
                if (request.getSession().getAttribute("state") == null) {
                    request.getSession().setAttribute("state", "initialState");
                }

                String state = (String)request.getSession().getAttribute("state");
                if (state.equals("initialState")) {
                    boolean isAuthenticated = (request.getUserPrincipal() != null);
                    if (isAuthenticated) {
                        // This servlet is protected by the web contianer or policy
                        // agent. To have gotten here, the user must have logged in
                        // successfully.
                        String principalName = request.getUserPrincipal().getName();
                        String guestPrincipalName = ProviderProperties.getInstance().getProperty
                            ("jaxr-ebxml.security.guestPrincipalName");
                        if (guestPrincipalName == null) {
                            log.error("The 'jaxr-ebxml.security.guestPrincipalName' property has not " +
                                "been set in the ebxmlrr web client servlet's jaxr-ebxml.properties file. " +
                                "This property must be set if the servlet is deployed with security " +
                                "constraints.");
                            // TO DO: For internationalization & ui customization
                            // purposes, the following should be moved to an
                            // included jsp/html file.
                            response.setContentType("text/html");        
                            ServletOutputStream out = response.getOutputStream();
                            out.println("<html><body>An error occured while " +
                                "accessing the ebxmlrr web client. The web client " +
                                "servlet is not correctly configured. See the " +
                                "ebxmlrr server log for details.</body></html>");
                        }
                        else if (principalName.equals(guestPrincipalName)) {
                            request.getSession().setAttribute("state", "processingRequest");
                            request.getRequestDispatcher("/welcome.html").include(request, response);
                        }
                        else {
                            Set credentials = getCredentials(principalName);
                            if ((credentials == null) || credentials.isEmpty()) {
                                credentials = generateCredentials(principalName);
                            }
                            request.getSession().setAttribute("credentials", credentials);
                            boolean isRegistered = (findUserByPrincipalName(principalName) != null);
                            if (isRegistered) {
                                request.getSession().setAttribute("state", "processingRequest");
                                request.getRequestDispatcher("/welcome.html").include(request, response);
                            }
                            else {
                                // Return the self-registration form to the browser.
                                request.getSession().setAttribute("state", "gatheringRegistrationInfo");
                                getServletConfig().getServletContext()
                                                  .getRequestDispatcher("/SelfRegistration.jsp")
                                                  .forward(request, response);
                            }
                        }
                    }
                    else {
                        // This servlet is not being protected by the web container
                        // or policy agent.
                        String principalName = ProviderProperties.getInstance().getProperty
                            ("jaxr-ebxml.security.anonymousUserKeystoreAlias");
                        if (principalName != null) {
                            Set credentials = getCredentials(principalName);
                            if ((credentials != null) && !credentials.isEmpty()) {
                                request.getSession().setAttribute("credentials", credentials);
                            }
                        }
                        request.getSession().setAttribute("state", "processingRequest");
                        request.getRequestDispatcher("/welcome.html").include(request, response);
                    }
                }
                else if (state.equals("registering")) {
                    setCredentials((Set)request.getSession().getAttribute("credentials"));
                    // use request parameters set by SelfRegistration form to 
                    // create a registry user.
                    createUser(request);
                    request.getSession().setAttribute("state", "processingRequest");
                    request.getRequestDispatcher("/welcome.html").include(request, response);
                }
                else if (state.equals("processingRequest")) {

                    setCredentials((Set)request.getSession().getAttribute("credentials"));

                    response.setContentType("text/html");        
                    ServletOutputStream out = response.getOutputStream();
                    String requestString = null;
                    try {
                        if (request.getParameter("ObjectType") != null) {
                            requestString = assembleSoapMessageFromParams(request);
                        }
                        else {
                            // Create SOAPMessage
                            requestString = getRequestStringFromRequest(request);
                        }
                    }
                    catch (IOException ex) {
                        out.println("<HTML><BODY>Error in Request ");
                        out.println(ex.getMessage());
                        out.println("</BODY></HTML>");
                    }

                    // Send the soap request to the registry. If credentials have
                    // been set on the connection, the request will be signed.
                    RegistryResponseHolder resp  = soapMessenger.sendSoapRequest(requestString, null);
                    BulkResponseImpl bResponse = new BulkResponseImpl(
                        (LifeCycleManagerImpl)connection.getRegistryService().getBusinessLifeCycleManager(), 
                        resp.getRegistryResponse(),  null);

                    // check for errors
                    Collection exceptions = bResponse.getExceptions();
                    if (exceptions != null) {
                        Iterator iter = exceptions.iterator();
                        Exception exception = null;
                        out.println("<HTML><BODY><br>Error in Request<br>");
                        while (iter.hasNext())  {
                            exception = (Exception) iter.next();
                            out.println("<br>"+exception.getMessage()+"<br>");
                        }                
                        out.println("</BODY></HTML>");
                        return;
                    }

                    if (requestString.indexOf("SubmitObjectsRequest") != -1) {
                        out.println("<HTML><BODY><h3>Status of SubmitObjectsRequest:<h3>");
                        out.println("<hr>");
                        out.println("SUCCESS");
                        out.println("<br>Request Id:<br>");
                        out.println(bResponse.getRequestId());
                        out.println("</BODY></HTML>");
                    }
                    else if (requestString.indexOf("AdhocQueryRequest") != -1) {
                        Collection registryObjects = bResponse.getCollection();
                        Concept commonObjectType = getCommonObjectType(registryObjects);
                        ObjectTypeConfig otCfg = getObjectTypeConfig(commonObjectType);
                        SearchResultsConfigType srCfg = otCfg.getSearchResultsConfig();
                        List srCols = srCfg.getSearchResultsColumn();
                        ProtoSearchResultsBean srBean = populateSearchResultsBean(bResponse);
                        displayResults(srBean, response);
                        /* NOT NOW
                        request.setAttribute("data", srBean);
                        RequestDispatcher rd =
                            getServletContext().getRequestDispatcher("/jsp/ebxmlrr.jsp");
                        rd.forward(request, response);
                         */
                    }
                }  
            }
            finally {
                connection.logoff();
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
            log.error("Error during request processing: ", t);
            throw new ServletException(t.getMessage());
        }
    }
    
    /** Wrapper for ConnectionImpl.setCredentials() that ignores null or
      * empty credential sets.
      */
    private void setCredentials(Set credentials) throws JAXRException {
        if ((credentials != null) && !credentials.isEmpty()) {
            connection.setCredentials(credentials);
        }
    }
    
    /** Get the credentials for the specified principal.
      *
      * @param alias
      *     The principal of the user making the request on the registry.
      *     This value will be obtained from the web container hosting the
      *     registry client by calling HttpServletRequest.getUserPrincipal().
      *     If this value is <code>null</code>, or if <code>principal.getName()</code>
      *     is <code>null</code>, then the default principal name, as specified
      *     by the <i>jaxr-ebxml.security.defaultPrincipalName</i> property
      *     will be used. If this property is not set, then an empty Set will
      *     be returned.
      * @return
      *     A Set of X500PrivateCredential objects representing the user's
      *     credentials. If this set is empty or null, no credentials will
      *     be passed to the registry with the request. The registry treats
      *     such requests as coming from the Registry Guest user.
      * @throws JAXRException
      *     Thrown if an error occurs while trying to map the principal
      *     to its credentials. An exception should not be thrown if there are
      *     no credentials associated with the principal. In this case, an
      *     empty Set should be returned.
      */
    public Set getCredentials(String alias) throws JAXRException {

        HashSet credentials = new HashSet();
        
        if (alias == null) {
            return credentials;
        }
        
        log.debug("Getting credentials for '" + alias + "'");

        try {
            credentials.add(SecurityUtil.getInstance().aliasToX500PrivateCredential(alias));
        }
        catch (JAXRException je) {
            // aliasToX500PrivateCredential() throws an exception if no certificate
            // can be found for the specified alias. For our purposes, this
            // is not an exception, so we just ignore such exceptions and
            // propogate all others.
            if (!je.getMessage().equals("Unknown alias in keystore") &&
                !je.getMessage().startsWith("KeyStore file not found")) 
            {
                throw je;
            }
            else {
                log.warn("Failed to get credentials for '" + alias + "'.\n" +
                    "Exception: " + je.getMessage());
            }
        }

        return credentials;
    }
    
    /** Generate a key pair and add it to the keystore.
      *
      * @param alias
      * @return
      *     A HashSet of X500PrivateCredential objects.
      * @throws Exception
      */    
    private HashSet generateCredentials(String alias) throws JAXRException {
        
        try {

            HashSet credentials = new HashSet();

            // The keystore file is at ${jaxr-ebxml.home}/security/keystore.jks. If
            // the 'jaxr-ebxml.home' property is not set, ${user.home}/jaxr-ebxml/ is
            // used.
            File keyStoreFile = KeystoreUtil.getKeystoreFile();
            String storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass");
            String keypass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.keypass");

            log.debug("Generating key pair for '" + alias + "' in '" + keyStoreFile.getAbsolutePath() + "'");

// When run in S1WS 6.0, this caused some native library errors. It appears that S1WS
// uses different encryption spis than those in the jdk. 
//            String[] args = {
//                "-genkey", "-alias", uid, "-keypass", "keypass",
//                "-keystore", keyStoreFile.getAbsolutePath(), "-storepass",
//                new String(storepass), "-dname", "uid=" + uid + ",ou=People,dc=sun,dc=com"
//            };
//            KeyTool keytool = new KeyTool();
//            ByteArrayOutputStream keytoolOutput = new ByteArrayOutputStream();
//            try {
//                keytool.run(args, new PrintStream(keytoolOutput));
//            }
//            finally {
//                log.info(keytoolOutput.toString());
//            }
// To work around this problem, generate the key pair using keytool (which executes
// in its own vm. Note that all the parameters must be specified, or keytool prompts
// for their values and this 'hangs'
            String[] cmdarray = {
                "keytool", 
                "-genkey", "-alias", alias, "-keypass", keypass,
                "-keystore", keyStoreFile.getAbsolutePath(), 
                "-storepass", storepass, "-dname", "cn=" + alias
            };
            Process keytool = Runtime.getRuntime().exec(cmdarray);
            try {
                keytool.waitFor();
            }
            catch (InterruptedException ie) {
            }
            if (keytool.exitValue() != 0) {
                throw new JAXRException("keytool command failed. Exit status: " + keytool.exitValue());
            }
            log.debug("Key pair generated successfully.");

            // After generating the keypair in the keystore file, we have to reload
            // SecurityUtil's KeyStore object.
            KeyStore keyStore = SecurityUtil.getInstance().getKeyStore();
            keyStore.load(new FileInputStream(keyStoreFile), storepass.toCharArray());

            credentials.add(SecurityUtil.getInstance().aliasToX500PrivateCredential(alias));

            return credentials;
        }
        catch (Exception e) {
            if (e instanceof JAXRException) {
                throw (JAXRException)e;
            }
            else {
                throw new JAXRException(e);
            }
        }
    }
    
    /**
      *
      * @param principalName
      * @throws JAXRException
      * @return
      */    
    private User findUserByPrincipalName(String principalName) throws JAXRException {
        User user = null;
        DeclarativeQueryManager dqm = bqm.getRegistryService().getDeclarativeQueryManager();
        String queryString = 
            "SELECT * " + 
            "FROM user_ u, slot s " +
            "WHERE u.id = s.parent AND s.name_='PrincipalName' AND value='" + principalName + "'";
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryString);
        BulkResponse br = dqm.executeQuery(query);
        Iterator results = br.getCollection().iterator();
        while (results.hasNext()) {
            user = (User)results.next();
            break;
        }
        return user;
    }

    /** Create a registry User object and populate its attributes.
      *
      * @param uid
      * @return
      *     An ebxml User object representing the user created.
      * @throws
      */
    private void createUser(HttpServletRequest request) throws JAXRException {

        RegistrationInfoBean regInfo = (RegistrationInfoBean)request.getSession().getAttribute("registrationInfoBean");
        String name = regInfo.getName();
        String firstName = regInfo.getFirstName();
        String middleName = regInfo.getMiddleName();
        String lastName = regInfo.getLastName();
        String emailAddress = regInfo.getEmailAddress();
        String emailAddressType = regInfo.getEmailAddressType();
        String description = regInfo.getDescription();
        String countryCode = regInfo.getCountryCode();
        String areaCode = regInfo.getAreaCode();
        String phoneNumber = regInfo.getPhoneNumber();
        String phoneURL = regInfo.getPhoneURL();
        String phoneType = regInfo.getPhoneType();
        String streetNumber = regInfo.getStreetNumber();
        String street = regInfo.getStreet();
        String city = regInfo.getCity();
        String stateOrProvince = regInfo.getStateOrProvince();
        String country = regInfo.getCountry();
        String postalCode = regInfo.getPostalCode();
        String postalAddressType = regInfo.getPostalAddressType();
        
        User user = blcm.createUser();
        
        InternationalString intlName = (InternationalStringImpl)user.getName();
        if (intlName == null) {
            intlName = (InternationalStringImpl)user.getLifeCycleManager().createInternationalString(name);
        }
        else {
            intlName.setValue(name);
        }
        user.setName(intlName);

        InternationalString intDescription = new InternationalStringImpl(blcm);
        intDescription.setValue(description);
        user.setDescription(intDescription);

        // Store the principal name in a slot associated with the user.
        // TO DO: this should be stored in an external identifier.
        Slot prinicipalNameSlot = blcm.createSlot("PrincipalName", request.getUserPrincipal().getName(), "String");
        user.addSlot(prinicipalNameSlot);

        Set postalAddresses = new HashSet();
        postalAddresses.add(user.getLifeCycleManager().createPostalAddress
            (streetNumber, street, city, stateOrProvince, country, postalCode, postalAddressType));
        user.setPostalAddresses(postalAddresses);
        
        PersonName personName = user.getPersonName();
        if (personName != null) {
            personName.setFirstName(firstName);
            personName.setMiddleName(middleName);
            personName.setLastName(lastName);
        } 
        else {
            personName = user.getLifeCycleManager().createPersonName(firstName, middleName, lastName);
            user.setPersonName(personName);
        }

        Set telephoneNumbers = new HashSet();
        TelephoneNumber telephoneNumber = user.getLifeCycleManager().createTelephoneNumber();
        telephoneNumber.setCountryCode(countryCode);
        telephoneNumber.setAreaCode(areaCode);
        telephoneNumber.setNumber(phoneNumber);
        telephoneNumber.setType("Office Phone");
        telephoneNumber.setUrl(phoneURL);
        telephoneNumber.setType(phoneType);
        telephoneNumbers.add(telephoneNumber);
        user.setTelephoneNumbers(telephoneNumbers);
        
        Set emailAddresses = new HashSet();
        emailAddresses.add(user.getLifeCycleManager().createEmailAddress(emailAddress, emailAddressType));
        user.setEmailAddresses(emailAddresses);

        log.debug("Saving User object in registry.");
        ArrayList objects = new ArrayList();
        objects.add(user);
        BulkResponse resp = blcm.saveObjects(objects);
        if ((resp != null) &&
            (resp.getStatus() != JAXRResponse.STATUS_SUCCESS)) 
        {
            Collection exceptions = resp.getExceptions();
            if (exceptions != null) {
                Iterator iter = exceptions.iterator();
                while (iter.hasNext()) {
                    Exception e = (Exception)iter.next();
                    log.error(e.getMessage(), e);
                }
                throw new JAXRException("Failed to create registry user. See log for details.");
            }
        }
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, 
                          HttpServletResponse response)
    throws ServletException, IOException
    {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo()
    {
        return "Short description";
    }
    
    private String getRequestStringFromRequest(HttpServletRequest request)
    throws IOException
    {
        String s = null;
        Reader reader = request.getReader();
        char[] buf = new char[request.getContentLength()];
        reader.read( buf );
        s = new String(buf);
        return s;
    }
    
    private String assembleSoapMessageFromParams(HttpServletRequest request)
    {
        StringBuffer soapMessage = new StringBuffer(beginMessage);
        
        StringBuffer sqlSelect = new StringBuffer("SELECT * FROM ");
        StringBuffer sqlWhere = new StringBuffer(SQL_WHERE);
        String objectType = (String)request.getParameter("ObjectType");
        if (objectType.indexOf("/") != -1) {
            handleClassifiedObjects(objectType, sqlSelect, sqlWhere);
        } else {
            sqlSelect.append(objectType).append(" ptn");
        }
        String caseSensitive = (String)request.getParameter("CaseSensitive");
        String name = (String)request.getParameter("Name");
        if (name != null && ! name.equals(""))
        {      
            handleName(name, sqlSelect, sqlWhere, caseSensitive);
        }
        String description = (String)request.getParameter("Description");
        if (description != null && ! description.equals(""))
        {      
            handleDescription(description, sqlSelect, sqlWhere, caseSensitive);
        }
        String[] classNodes = request.getParameterValues("Classifications");
        if (classNodes != null)
        {
            for (int i = 0; i < classNodes.length; i++) {
                handleClassNodes(classNodes[i], sqlWhere);
            }
        }
        soapMessage.append(sqlSelect.toString());
        if (! (sqlWhere.toString()).equals(SQL_WHERE)) {
            soapMessage.append(sqlWhere.toString());
        }
        soapMessage.append(endMessage);
        String query = soapMessage.toString();
        log.info("sql query: "+query);
        
        return query;
    }
    
    private void handleName(String name, StringBuffer sqlSelect,
                                         StringBuffer sqlWhere,
                                         String caseSensitive) {
        sqlSelect.append(", name n ");
        if (! (sqlWhere.toString()).equals(SQL_WHERE)) {
            sqlWhere.append(" AND ");
        }
        sqlWhere.append("(");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append("UPPER(");
        }
        sqlWhere.append("n.value");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append(")");
        }
        sqlWhere.append(" LIKE ");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append("UPPER(");
        }
        sqlWhere.append("'");
        sqlWhere.append(name);
        sqlWhere.append("'");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append(")");
        }
        sqlWhere.append(" AND n.parent = ptn.id) ");
    }
    
    private void handleDescription(String description, 
                                    StringBuffer sqlSelect,
                                    StringBuffer sqlWhere,
                                    String caseSensitive) {
        sqlSelect.append(", description d ");
        if (! (sqlWhere.toString()).equals(SQL_WHERE)) {
            sqlWhere.append(" AND ");
        }
        sqlWhere.append("(");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append("UPPER(");
        }
        sqlWhere.append("d.value");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append(")");
        }
        sqlWhere.append(" LIKE ");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append("UPPER(");
        }
        sqlWhere.append("'");
        sqlWhere.append(description);
        sqlWhere.append("'");
        if (caseSensitive == null || caseSensitive.equals("")) {
            sqlWhere.append(")");
        }
        sqlWhere.append(" AND d.parent = ptn.id) ");
    }
    
    private void handleClassifiedObjects(String objectType, 
                                         StringBuffer sqlSelect,
                                         StringBuffer sqlWhere) {
        String[] tokens = objectType.split("/", 2);
        sqlSelect.append(tokens[0]).append(" ptn, ClassificationNode typeNode");
        sqlWhere.append("((ptn.objectType = typeNode.id) AND ");
        sqlWhere.append("(typeNode.path LIKE '%/").append(tokens[1]);
        sqlWhere.append("%'))");
    }
    
    private void handleClassNodes(String classNode, StringBuffer sqlWhere) {
        if (! sqlWhere.toString().equals(SQL_WHERE)) {
            sqlWhere.append(" AND ");
        }
        sqlWhere.append("(ptn.id IN (SELECT classifiedObject FROM ");
        sqlWhere.append("Classification WHERE classificationNode IN ");
        sqlWhere.append("(SELECT id FROM ClassificationNode WHERE path LIKE ");
        sqlWhere.append("'/").append(classNode).append("%')))");
        /*
         *(ptn.id IN (SELECT classifiedObject FROM Classification WHERE
         classificationNode IN ( SELECT id FROM ClassificationNode WHERE path
         LIKE '/urn:uuid:8979d7f2-472a-4309-9879-fbc341d047d4/Prototype%')))
         */
    }
    
    private Concept getCommonObjectType(Collection registryObjects)
        throws JAXRException 
    {
        Concept commonType = null;
        String commonPath = null;

        Iterator iter = registryObjects.iterator();
        while (iter.hasNext()) {
            RegistryObject ro = (RegistryObject)iter.next();
            ConceptImpl type = (ConceptImpl)ro.getObjectType();

            String path = type.getPath();

            if (commonPath == null) {
                commonPath = path;
            } 
            else {
                if (!(commonPath.equals(path))) {
                    //Determine common base type for both:
                    if (commonPath.startsWith(path + "/")) {
                        //The new type is a baseType of current commonType
                        //Set commonType to new type
                        commonPath = path;
                    } else if (path.startsWith(commonPath + "/")) {
                        //The current commonType is a baseType of new type
                        //Leave commonType unchanged
                        continue;
                    } else {
                        //The current commonType and new type 
                        //do not have an ancestor/descendant relationship
                        //Find a common base type between them.
                        String smallerPath = commonPath;
                        String biggerPath = path;
                        boolean swap = false;

                        if (commonPath.length() > path.length()) {
                            smallerPath = path;
                            biggerPath = commonPath;
                            swap = true;
                        }

                        int len = smallerPath.length();

                        for (int i = 0; i < len; i++) {
                            if (smallerPath.charAt(i) != biggerPath.charAt(i)) {
                                commonPath = smallerPath.substring(0,
                                        smallerPath.lastIndexOf('/', i));

                                break;
                            }
                        }
                    }
                }
            }
        }

        if (commonPath == null) {
            //Use RegistryObject path
            commonPath = "/urn:uuid:3188a449-18ac-41fb-be9f-99a1adca02cb/RegistryObject";
        }

        commonType = (Concept) bqm.findConceptByPath(commonPath);

        return commonType;
    }
    
    private ObjectTypeConfig getObjectTypeConfig(Concept commonObjectType)
        throws JAXRException {
        ObjectTypeConfig cfg = null;

        while (true) {
            //Now get the ObjectTypeConfig for the commonObjectType
            cfg = (ObjectTypeConfig) objectTypeToConfigMap.get(commonObjectType.getKey()
                                                                               .getId());

            if (cfg != null) {
                break;
            } else {
                Concept parent = commonObjectType.getParentConcept();

                if (parent == null) {
                    //Use RegistryObject (id urn:uuid:a7ec3db9-9342-4016-820c-cff66c0bb021) as default objectType
                    cfg = (ObjectTypeConfig) objectTypeToConfigMap.get(
                            "urn:uuid:a7ec3db9-9342-4016-820c-cff66c0bb021");

                    break;
                } else {
                    commonObjectType = parent;
                }
            }
        }
        return cfg;
    }
    
    private ProtoSearchResultsBean populateSearchResultsBean(BulkResponseImpl bResponse)
        throws ClassNotFoundException, NoSuchMethodException, 
        IllegalArgumentException, IllegalAccessException, 
        InvocationTargetException, ExceptionInInitializerError, JAXRException {
        
        Collection registryObjects = bResponse.getCollection();
        Iterator roItr = registryObjects.iterator();
        Concept commonObjectType = getCommonObjectType(registryObjects);
        String objectType = commonObjectType.getValue();
        ObjectTypeConfig otCfg = getObjectTypeConfig(commonObjectType);         
        SearchResultsConfigType srCfg = otCfg.getSearchResultsConfig();
        List srCols = srCfg.getSearchResultsColumn();
        int numCols = srCols.size();
        String[] columnHeaders = new String[numCols];
        // Replace ObjectType with Id. TODO - formalize this convention
        for (int i = 0; i < numCols; i++) {
            SearchResultsColumnType srCol = 
                (SearchResultsColumnType) srCols.get(i);
            if (i == 0)
                columnHeaders[i] = "Id";
            else
                columnHeaders[i] = srCol.getColumnHeader();
        }
        String className = otCfg.getClassName();
        List columnValues = new ArrayList();
        while (roItr.hasNext()) {
            RegistryObject registryObject = (RegistryObject)roItr.next();
            Object[] columnValuesRow = new Object[numCols];
            // Replace data with link to Id. TODO - formalize this convention
            for (int i = 0; i < numCols; i++) {
                if (i == 0) {
                    Key key = registryObject.getKey();
                    String id = key.getId();
                    // TODO: make the omar host configurable
                    String link2Id = "<a href=\""+
                        restUrl+"?"+
                        "interface=QueryManager&method=getRegistryObject&"+
                        "param-id="+id+"&flavor=text/html\" target=\"new\">Id</a>";
                    columnValuesRow[i] = link2Id;
                } else {
                    SearchResultsColumnType srCol = (SearchResultsColumnType) 
                        srCols.get(i);
                    Object columnValue = getColumnValue(srCol, className, 
                        registryObject);
                    columnValuesRow[i] = columnValue;
                }
            }
            columnValues.add(columnValuesRow);
        }
        return new ProtoSearchResultsBean(objectType, columnHeaders, columnValues);
    }
    
    private Object getColumnValue(SearchResultsColumnType srCol, 
                                     String className,
                                     RegistryObject registryObject) 
        throws ClassNotFoundException, NoSuchMethodException, 
        IllegalArgumentException, IllegalAccessException, 
        InvocationTargetException, ExceptionInInitializerError, JAXRException {
    
        Object value = null;
        String methodName = srCol.getMethod();
        Class clazz = Class.forName(className);

        List params = srCol.getMethodParameter();
        int numParams = params.size();
        Class[] parameterTypes = new Class[numParams];
        Object[] parameterValues = new Object[numParams];
        Iterator paramsIter = params.iterator();
        int i = 0;

        //Setup parameterTypes
        while (paramsIter.hasNext()) {
            MethodParameter mp = (MethodParameter) paramsIter.next();
            String paramTypeName = mp.getType();
            parameterTypes[i] = Class.forName(paramTypeName);

            String paramValue = mp.getValue();
            parameterValues[i] = paramValue;

            i++;
        }

        Method method = clazz.getMethod(methodName, parameterTypes);

        //Invoke method to get Value as object. Convert the object to a format suitable for display
        value = method.invoke(registryObject, parameterValues);
        // handle value returned from method.invoke(...)
        if (value == null) {
            value = "";
        } else {
            value = convertValue(value);
            if (value instanceof Collection) {
                value = formatCollectionString((Collection)value);
            }
        }
        return value;
    }
    
    public Object convertValue(Object value) {
        Object finalValue = null;
        Locale selectedLocale = Locale.getDefault();

        try {
            if (value instanceof InternationalString) {
                finalValue = ((InternationalStringImpl) value).getClosestValue(selectedLocale);
            } else if (value instanceof ExternalLink) {
                finalValue = ((ExternalLink) value).getExternalURI();

                try {
                    URL url = new URL(((ExternalLink) value).getExternalURI());
                    finalValue = url;
                } catch (MalformedURLException e) {
                }
            } else if (value instanceof Collection) {
                //Converts elements of Collection
                Collection c1 = (Collection) value;
                Collection c2 = new ArrayList();
                Iterator iter = c1.iterator();

                while (iter.hasNext()) {
                    c2.add(convertValue(iter.next()));
                }

                finalValue = c2;
            } else if (value instanceof Slot) {
                Collection c = ((Slot) value).getValues();
                finalValue = c;
            } else if (value instanceof Concept) {
                finalValue = ((Concept) value).getValue();
            } else {
                finalValue = value;
            }
        } catch (JAXRException e) {
            log.error(e);
        }

        return finalValue;
    }
    
    private String formatCollectionString(Collection collection) {
        String value = collection.toString();
        StringBuffer sb3 = new StringBuffer(value);
        // remove leading straight bracket '['
        sb3.deleteCharAt(0);
        // remove ending straight bracket ']'
        sb3.deleteCharAt(sb3.length()-1);
        value = sb3.toString();
                
        StringBuffer sb = new StringBuffer();
        String[] tokens = value.split(", ");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) {
                sb.append(", ");
            }
            // construct hyperlink
            if (token.indexOf("://") != -1) {
                StringBuffer sb2 = new StringBuffer();
                sb2.append("<a href=\"");
                sb2.append(token);
                sb2.append("\" target=\"_new\">");
                sb2.append(token);
                sb2.append("</a>");
                token = sb2.toString();
            }
            sb.append(token);
        }
        value = sb.toString();
        
        return value;
    }    
  
    private void displayResults(ProtoSearchResultsBean srBean, 
        HttpServletResponse response) throws IOException {
        ServletOutputStream out = response.getOutputStream();
        out.println("<html>");

        out.println("<body bgcolor=\"#FFFFFF\">");
        out.println("<p><h3>Search Results</h3>");
        out.println("<hr>");  
        out.println("<P STYLE=\"margin-right: 20.0 cm\">");
        List columnValues = srBean.getColumnValues();
        if (columnValues.size() == 0) {
            out.println("<h3>No results found</h3>");
        }
        else {
            out.println("<h3>Object Type: "+srBean.getObjectType()+"</h3>"); 
            out.println("<font face=\"sans-serif\" size=\"1\">");
            out.println("<center>");
            out.println("<table width=\"100%\" border=\"0\">");
            out.println("<tr>");
            String[] headers = srBean.getColumnHeaders();
            for (int j = 0; j < headers.length; j++)
            {
                out.println("<th bgcolor=\"9999CC\">"+
                    (String)headers[j]+"</th>");
            }
            out.println("</tr>");
            int numRows = columnValues.size();
            for (int i = 0; i < numRows; i++) {
                Object[] values = (Object[])columnValues.get(i);
                out.println("<tr>");
                for (int j = 0; j < values.length; j++) {
                    out.println("<td bgcolor=\"#CCCCFF\">"+values[j]+"</td>");
                }
                out.println("</tr>");
            }
            out.println("</table>");
            out.println("</center>");
        }
        out.println("</p>");
        out.println("</font>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private Configuration getConfiguration() throws IOException, JAXBException {
        Configuration cfg = null;
        InputStream is = null;
        File cfgFile = null;
        boolean readCfgFromHome = true;
        String jaxrHome = null;

        jaxrHome = ProviderProperties.getInstance().getProperty("jaxr-ebxml.home");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            File jaxrHomeDir = new File(jaxrHome);

            if (!jaxrHomeDir.exists()) {
                jaxrHomeDir.mkdir();
            }

            cfgFile = new File(jaxrHomeDir, "registry-browser-config.xml");

            if (!cfgFile.canRead()) {
                URL cfgFileUrl = getClass().getResource("/org/freebxml/omar/client/ui/common/conf/config.xml");
                bis = new BufferedInputStream(cfgFileUrl.openStream());
                bos = new BufferedOutputStream(new FileOutputStream(
                            cfgFile));

                byte[] buffer = new byte[1024];
                int bytesRead = 0;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException ioe) {
            readCfgFromHome = false;
            log.warn("Unable to store browser configuration in user home");

            //                displayError("Unable to store browser configuration in user home",
            //                             ioe);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }

            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                }
            }
        }

        // Now get the configuration
        try {
            if (readCfgFromHome) {
                is = new FileInputStream(cfgFile);
            } else {
                URL cfgFileUrl = getClass().getResource("conf/config.xml");
                is = cfgFileUrl.openStream();
            }

            Unmarshaller unmarshaller = (new ObjectFactory()).createUnmarshaller();
            cfg = (Configuration) unmarshaller.unmarshal(is);
        } catch (IOException e) {
            throw e;
        } catch (JAXBException e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return cfg;
    }

}
