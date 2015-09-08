/*
 * Copyright (c) 2003 - 2009 OpenSubsystems s.r.o. Slovak Republic. All rights reserved.
 * 
 * Project: OpenSubsystems
 * 
 * $Id: WebSessionServlet.java,v 1.1 2009/04/22 05:29:29 bastafidli Exp $
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License. 
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */

package org.opensubsystems.core.www.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opensubsystems.core.error.OSSException;
import org.opensubsystems.core.util.CallContext;
import org.opensubsystems.core.util.ClassUtils;
import org.opensubsystems.core.util.Config;
import org.opensubsystems.core.util.GlobalConstants;
import org.opensubsystems.core.util.Log;
import org.opensubsystems.core.util.MultiConfig;
import org.opensubsystems.core.util.PropertyUtils;
import org.opensubsystems.core.util.StopWatch;
import org.opensubsystems.core.util.servlet.WebSessionUtils;
import org.opensubsystems.core.util.servlet.WebUtils;

/**
 * Base class for all servlets developed as part of this project. It's main 
 * responsibility is to provide session information if required therefore making 
 * sure that nobody who is not logged in (or otherwise authenticated) can proceed. 
 * This servlet intercepts all requests, makes sure that valid HTTP session is 
 * established and after credentials are verified, let the request proceed.
 * 
 * Developers are advised to derive all servlets from this servlet since this
 * helps us to establish effective security policy at a single place. 
 *
 * @version $Id: WebSessionServlet.java,v 1.1 2009/04/22 05:29:29 bastafidli Exp $
 * @author Miro Halas
 * @code.reviewer Miro Halas
 * @code.reviewed 1.25 2008/11/13 23:57:09 bastafidli
 */
public class WebSessionServlet extends HttpServlet
{
   /**
    * This interface should be implemented by class which can validate session 
    * information. The implementing class is consulted for each request to make
    * sure that the the user represented by the session is allowed to issue 
    * requests to the server.
    */
   public static interface SessionValidator
   {
      /**
       * Check if the session information is still valid. Valid session means that
       * the session is authentic and was not yet invalidated.
       *
       * @param strSessionId - Session information to check
       * @return boolean - if true the session is still valid and the user 
       *                   represented by this session is allowed to issue requests
       *                   to the server
       * @throws OSSException - error during checking
       */
      boolean checkSession(
         String strSessionId
      ) throws OSSException;   
   }

   // Configuration settings ///////////////////////////////////////////////////

   /** 
    * Configuration setting specifying if the web tier should cache the request 
    * dispatchers used to dispatch client requests to various web resources. 
    * Caching of the request dispatchers can improve performance, but not all 
    * web containers allows to do it. Jetty allows this, some version of 
    * Weblogic don't.
    * @see #WEBSESSION_DISPATCHER_CACHED_DEFAULT
    */   
   public static final String WEBSESSION_DISPATCHER_CACHED 
                                 = "oss.webserver.dispatcher.cached";

   /** 
    * Configuration setting specifying if the server must ensure that the client
    * accepted the server session before it allows further communication. This
    * improves the login process if one is needed since at the time of login
    * the server already knows that the client supports and accepted its session
    * tracking.
    * @see #WEBSESSION_HADSHAKE_REQUIRED_DEFAULT
    */   
   public static final String WEBSESSION_HANDSHAKE_REQUIRED 
                                 = "oss.webserver.sessionhandshake.required";

   /** 
    * Configuration setting specifying the URL of handshake page to which user 
    * will be redirected if handshake is required and session wasn't confirmed
    * at the time when the request is submitted to the server.
    */   
   public static final String WEBSESSION_HANDSHAKE_URL 
                                 = "oss.webserver.sessionhandshake.url";

   /** 
    * Configuration setting specifying if user has to be logged in in order to 
    * process his or her request sent to the server.
    * @see #WEBSESSION_LOGIN_REQUIRED_DEFAULT
    */   
   public static final String WEBSESSION_LOGIN_REQUIRED 
                                 = "oss.webserver.login.required";

   /** 
    * Configuration setting specifying the URL of login page to which user will 
    * be redirected if login is required and user is not logged in at the time
    * when request is submitted to the server.
    */   
   public static final String WEBSESSION_LOGIN_URL = "oss.webserver.login.url";

   /** 
    * Configuration setting specifying name of the class implementing 
    * SessionValidator interface to verify validity of a session each time a 
    * request is submitted to the server. 
    */   
   public static final String SESSION_VALIDATOR_CLASS 
                                 = "oss.webserver.sessionvalidator.class";

   /** 
    * Configuration setting specifying if the login page should be displayed
    * in a secure mode using SSL protocol. 
    * @see #m_bLoginSecure
    * @see #DEFAULT_LOGIN_SECURE
    */   
   public static final String LOGIN_SECURE = "oss.login.secure";   

   /** 
    * Configuration setting specifying if all pages of the application should be 
    * displayed in a secure mode using SSL protocol.
    * @see #m_bApplicationSecure
    * @see #DEFAULT_APPLICATION_SECURE
    */   
   public static final String APPLICATION_SECURE = "oss.application.secure";   

   // Constants ////////////////////////////////////////////////////////////////

   /**
    * Default value for WEBSESSION_DISPATCHER_CACHED.
    * By default don't cache them since this will work on any servlet engine.
    * @see #WEBSESSION_DISPATCHER_CACHED
    */
   public static final Boolean WEBSESSION_DISPATCHER_CACHED_DEFAULT 
                                  = Boolean.FALSE;

   /**
    * Default value for WEBSESSION_HANDSHAKE_REQUIRED
    * @see #WEBSESSION_HANDSHAKE_REQUIRED
    */
   public static final Boolean WEBSESSION_HADSHAKE_REQUIRED_DEFAULT 
                                  = Boolean.FALSE;
   
   /**
    * Default value for WEBSESSION_LOGIN_REQUIRED
    * @see #WEBSESSION_LOGIN_REQUIRED
    */
   public static final Boolean WEBSESSION_LOGIN_REQUIRED_DEFAULT 
                                  = Boolean.FALSE;

   /**
    * Name of the HTTP session object storing path where to continue after 
    * login.
    */
   public static final String LOGIN_FORWARD_SESSION_PARAM = "login.forward";

   /**
    * Full URL how this servlet was invoked so that GUI can use it for 
    * callbacks.
    */
   public static final String SERVLET_PATH_REQUEST_PARAM = "servletpath";

   /**
    * Parameter which must be specified in URL if the user should be attached
    * to this server after it was logged in at some other server.  
    */
   public static final String ATTACH_INTERNAL_SESSION_ID_URL_PARAM 
                                 = "osssessionid";

   /**
    * Default value for LOGIN_SECURE.
    * @see #m_bLoginSecure
    * @see #LOGIN_SECURE
    */
   public static final Boolean DEFAULT_LOGIN_SECURE = Boolean.FALSE;

   /**
    * Default value for APPLICATION_SECURE.
    * @see #m_bApplicationSecure
    * @see #APPLICATION_SECURE
    */
   public static final Boolean DEFAULT_APPLICATION_SECURE 
                                  = DEFAULT_LOGIN_SECURE;

   // Cached values ////////////////////////////////////////////////////////////

   /**
    * Logger for this class
    */
   private static Logger s_logger = Log.getInstance(WebSessionServlet.class);

   /**
    * Flag specifying if caching of request dispatchers is enabled.
    * Jetty allows that, Weblogic doesn't.
    * @see #WEBSESSION_DISPATCHER_CACHED
    * @see #WEBSESSION_DISPATCHER_CACHED_DEFAULT
    */
   private boolean m_bRequestDispatcherCached;
   
   /**
    * True if client has to accept the session before it can proceed with 
    * further requests.
    * @see #WEBSESSION_HANDSHAKE_REQUIRED
    * @see #WEBSESSION_HADSHAKE_REQUIRED_DEFAULT
    */
   private boolean m_bHandhakeRequired;
   
   /**
    * URL of the handshake page in case handshake is required.
    */
   protected String m_strHandshakeURL;
   
   /**
    * True if user has to be logged in to process request by this servlet
    * @see #WEBSESSION_LOGIN_REQUIRED
    * @see #WEBSESSION_LOGIN_REQUIRED_DEFAULT
    */
   private boolean m_bLoginRequired;
   
   /**
    * URL of the login page in case login is required.
    */
   private String m_strLoginURL;   
   
   /**
    * Servlet context.
    */
   protected ServletContext m_scServletContext;
   
   /**
    * Servlet configuration. 
    */
   protected ServletConfig m_scConfig;
   
   /**
    * Initial parameters from servlet configuration converted into properties 
    * object. 
    */
   protected Properties m_configProperties;
   
   /**
    * If not null, then this instance will be used to validate session for each
    * request.
    */
   protected SessionValidator m_sessionValidator;

   /**
    * Flag signaling if login will be processed using SSL.
    * @see #LOGIN_SECURE
    * @see #DEFAULT_LOGIN_SECURE
    */
   protected boolean m_bLoginSecure;

   /**
    * Flag signaling if whole application will be using SSL.
    * @see #APPLICATION_SECURE
    * @see #DEFAULT_APPLICATION_SECURE
    */
   protected boolean m_bApplicationSecure;

   // Attributes ///////////////////////////////////////////////////////////////
   
   /**
    * Generated serial version id for this class.
    */
   private static final long serialVersionUID = 322547463691937622L;

   // Servlet operations ///////////////////////////////////////////////////////

   /**
    * {@inheritDoc}
    */
   public final void init(
      ServletConfig scConfig
   ) throws ServletException
   {
      m_scConfig = scConfig;
      m_scServletContext = scConfig.getServletContext();
      m_configProperties = WebUtils.getInitParameters(scConfig);
      
      try
      {
         Properties prpSettings;

         ((MultiConfig)Config.getInstance()).setCurrentProperties(
                                                m_configProperties);

         prpSettings = Config.getInstance().getProperties();

         super.init(scConfig);
         init(scConfig, prpSettings);
      }
      catch (OSSException ossExc)
      {
         throw new ServletException("Unexpected exception.", ossExc);  
      }
      finally
      {
         try
         {
            ((MultiConfig)Config.getInstance()).resetCurrentProperties(
                                                   m_configProperties);
         }
         catch (OSSException ossExc)
         {
            throw new ServletException("Unexpected exception.", ossExc);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public final void destroy(
   )
   {
      super.destroy();
      
      try
      {
         destroy(m_scConfig);
      }
      catch (OSSException ossExc)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", ossExc);
         throw new RuntimeException("Unexpected exception.", ossExc);
      }
      catch (ServletException ossExc)
      {
         // No way to throw checked exception so convert it to unchecked 
         s_logger.log(Level.SEVERE, "Unexpected exception.", ossExc);
         throw new RuntimeException("Unexpected exception.", ossExc);
      }
   }

   /**
    * Main service routine for the servlet. 
    *
    * Subclasses can't override this method to make sure that the if-modified
    * logic is handled correctly (when they override getLastModified method). 
    * Only the doXXX method should be overridden. This servlet also makes sure 
    * (if configured that way), that nobody  who is not logged in can proceed.
    *
    * @param hsrqRequest  - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - an error has occurred while serving request
    * @throws IOException - an error has occurred while writing response
    */
   public final void service(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      StopWatch     timer = new StopWatch();
      HttpSession hsSession; // http session used to  maintain link between
                             // browser and server

      WebUtils.adjustPorts(hsrqRequest);
      hsSession = hsrqRequest.getSession(true);
      
      try
      {
         ((MultiConfig)Config.getInstance()).setCurrentProperties(
                                                m_configProperties);
         
         // Set up variable for server port number the application is currently 
         // running on
         
         // Capture incoming request, this can help us identify request which
         // didn't complete
         // Format is:
         // Thread name, time, in or out, time in user readable form, session ID
         StringBuffer sbBuffer = new StringBuffer();

         sbBuffer.append(Thread.currentThread().getName());
         sbBuffer.append(",in");
         sbBuffer.append(",");
         sbBuffer.append(timer.getStartTime());
         sbBuffer.append(",");
         sbBuffer.append((new Date(timer.getStartTime())).toString());
         sbBuffer.append(",");
         sbBuffer.append(WebSessionUtils.getSessionId(hsSession));
         sbBuffer.append(",");
         sbBuffer.append(hsSession.getId());
         sbBuffer.append(",");
         sbBuffer.append("0"); // no initial processing time
         s_logger.fine(sbBuffer.toString());

         // Save the URL how this servlet was invoked since if we forward it to
         // JSP, the getServletPath points to the JSP and not to the original
         // servlet (in Jetty 4.2.9)
         hsrqRequest.setAttribute(SERVLET_PATH_REQUEST_PARAM, 
                                  hsrqRequest.getContextPath() 
                                  + hsrqRequest.getServletPath());
         try
         {
            // Toggle URL (from http -> https or from https -> http)
            if (hsrqRequest.isSecure() != shouldRequestBeSecure())
            {
               String strOriginal;
               String stsRedirect;
               
               strOriginal = WebUtils.getFullRequestURL(hsrqRequest);
               stsRedirect = WebUtils.toggleSecure(hsrqRequest,
                                                   strOriginal,
                                                   shouldRequestBeSecure());
               s_logger.finer("Redirecting due to HTTP(S) from " + strOriginal 
                              + " to " + stsRedirect);
               // redirect to the toggled url
               hsrpResponse.sendRedirect(stsRedirect);
            }
            else
            {
               if ((m_bHandhakeRequired) && (hsSession.isNew()))
               {
                  s_logger.finest("Session " + hsSession.getId() 
                                  + " is still new and hanshake is required");
                  handleNewSession(hsSession, hsrqRequest, hsrpResponse);
               }
               else
               {
                  // The client has accepted session, or handshake wans't 
                  // required so proceed with servicing the request
                  Principal userCredentials;

                  // Find out if the client has already logged in. This is also 
                  // done if login is not required since  some applications 
                  // provide optional login and for those we want to establish 
                  // user's identity as well if user has already logged in
                  userCredentials = verifyLogin(hsSession, hsrqRequest, 
                                                hsrpResponse);
                  if ((m_bLoginRequired) && (userCredentials == null))
                  {
                     // User hasn't logged in yet. Store the requested URL in 
                     // the session object so that after login user can continue 
                     // to the requested page
                     saveLoginRedirect(hsSession, 
                                       WebUtils.getFullRequestURL(hsrqRequest));
            
                     // Give derived classes opportunity to handle any 
                     // functionality that is common to all requests
                     // We need to call this since if this is already request
                     // to a login page then redirectToLogin would just simply
                     // handle the login page as if it was a normal request
                     preservice(hsSession, hsrqRequest, hsrpResponse, false);
                     
                     // We cannot use RequestDispatcher because browser would 
                     // cache this login page as the requested page
                     redirectToLogin(hsrqRequest, hsrpResponse);
                  }
                  else
                  {
                     // If there is one associate user's identity with this 
                     // thread so that the app server can find out, who is 
                     // the current user Everything is OK, now service the 
                     // request We have to have try/finally block here so we 
                     // reset current user
                     if (userCredentials != null)
                     {
                        CallContext.getInstance().setCurrentUserAndSession(
                                       userCredentials, 
                                       WebSessionUtils.getSessionId(hsSession));
                     }
                  
                     // Give derived classes opportunity to handle any 
                     // functionality that is common to all requests
                     preservice(hsSession, hsrqRequest, hsrpResponse, true);
         
                     // Service the request by calling the base class method 
                     // so that getLastModified() method is correctly called
                     super.service(hsrqRequest, hsrpResponse);
                  }
               }
            }
         } 
         finally
         {
            CallContext.getInstance().reset();
         }
      }
      finally
      {
         try
         {
            ((MultiConfig)Config.getInstance()).resetCurrentProperties(
                                                        m_configProperties);
         }
         catch (OSSException ossExc)
         {
            throw new ServletException("Unexpected exception.", ossExc);
         }
         finally
         {
            StringBuffer sbBuffer = new StringBuffer();
            timer.stop();
   
            sbBuffer.append(Thread.currentThread().getName());
            sbBuffer.append(",out");
            sbBuffer.append(",");
            sbBuffer.append(timer.getStopTime());
            sbBuffer.append(",");
            sbBuffer.append((new Date(timer.getStopTime())).toString());
            sbBuffer.append(",");
            try
            {
               sbBuffer.append(WebSessionUtils.getSessionId(hsSession));
               sbBuffer.append(",");
               sbBuffer.append(hsSession.getId());
            }
            catch (IllegalStateException iseExc)
            {
               // This happen when the session was invalidated, don't even record 
               // it
               sbBuffer.append("invalidated,invalidated");
            }
            sbBuffer.append(",");
            sbBuffer.append(timer.getDuration());
            sbBuffer.append(",");
            sbBuffer.append(timer.toString());
            s_logger.fine(sbBuffer.toString());
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getServletInfo(
   )
   {
      return this.getClass().getName();
   }
   
   // Helper methods ///////////////////////////////////////////////////////////

   /**
    * Initialize the servlet. This method extends the default initialization 
    * of the servlet by providing entire web application related settings. 
    * 
    * @param scConfig - servlet configuration object passed by the container
    * @param prpSettings - application configuration settings reflecting also
    *                      settings from the servlet config
    * @throws ServletException - an error has occurred
    * @throws OSSException - an error has occurred.
    */
   protected void init(
      ServletConfig scConfig,
      Properties    prpSettings
   ) throws ServletException,
            OSSException
   {
      String strValidatorClass;
      
      m_bRequestDispatcherCached = PropertyUtils.getBooleanProperty(
                                      prpSettings, WEBSESSION_DISPATCHER_CACHED, 
                                      WEBSESSION_DISPATCHER_CACHED_DEFAULT, 
                                      "Should request dispatchers be cached"
                                   ).booleanValue();
      
      m_bHandhakeRequired = PropertyUtils.getBooleanProperty(
                               prpSettings, WEBSESSION_HANDSHAKE_REQUIRED, 
                               WEBSESSION_HADSHAKE_REQUIRED_DEFAULT, 
                               "Must the client accept the server session"
                               + " further communication is allowed"
                            ).booleanValue();
      
      // Empty URL is allowed since that can be the root of the web site
      m_strHandshakeURL = PropertyUtils.getStringProperty(
                             prpSettings, WEBSESSION_HANDSHAKE_URL, 
                             "URL of the handshake page", true);

      m_bLoginRequired = PropertyUtils.getBooleanProperty(
                            prpSettings, WEBSESSION_LOGIN_REQUIRED, 
                            WEBSESSION_LOGIN_REQUIRED_DEFAULT, 
                            "Does the user need to login before able to"
                            + " access the server").booleanValue();
      
      // Empty URL is allowed since that can be the root of the web site
      m_strLoginURL = PropertyUtils.getStringProperty(
                                       prpSettings, WEBSESSION_LOGIN_URL, 
                                       "URL of the login page", true);

      // Empty session validator is allowed since we don't have to validate  
      // the session
      strValidatorClass = PropertyUtils.getStringProperty(
                             prpSettings, SESSION_VALIDATOR_CLASS, null, 
                             "Class name of session validator", true);
      if ((strValidatorClass != null) && (strValidatorClass.length() > 0))
      {
         m_sessionValidator = (SessionValidator)ClassUtils.createNewInstance(
                                                   strValidatorClass);
         s_logger.finest("Instantiated session validator " 
                         + strValidatorClass);        
      }
      
      m_bLoginSecure = PropertyUtils.getBooleanProperty(
                          prpSettings, LOGIN_SECURE, DEFAULT_LOGIN_SECURE, 
                          "Should the login page be accessed using secure"
                          + " protocol").booleanValue();

      m_bApplicationSecure = PropertyUtils.getBooleanProperty(
               prpSettings, APPLICATION_SECURE, DEFAULT_APPLICATION_SECURE, 
               "Should the entire application be accessed using secure"
               + " protocol").booleanValue();
   }
   
   /**
    * Destroy the servlet. This method extends the default destruction of the 
    * servlet by providing the configuration object which was passed to the 
    * init. 
    * 
    * @param scConfig - servlet configuration object passed by the container
    *                   to the init method
    * @throws ServletException - an error has occurred
    * @throws OSSException - an error has occurred.
    */
   protected void destroy(
      ServletConfig scConfig
   ) throws ServletException,
            OSSException
   {
      // Nothing to do here but the derived classes my override this as
      // needed
   }
   
   /**
    * Check if caching of request dispatchers is enabled.
    * Jetty allows that, Weblogic doesn't.
    *
    * @return boolean
    */
   protected boolean isDispatcherCachingEnabled(
   )
   {
      return m_bRequestDispatcherCached;
   }

   /**
    * This function handles the scenarios, when the HTTP session generated for 
    * the client is still new and the client doesn't know about it yet. By 
    * default it just redirect to the handshake page. This method is called only 
    * when session handshake is required usually specified by configuration 
    * property.
    *
    * @param hsSession - HTTP session object
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - problem has occurred while processing request
    * @throws IOException - problem has occurred while processing request
    * @see #WEBSESSION_HANDSHAKE_REQUIRED
    */
   protected void handleNewSession(
      HttpSession         hsSession,
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException, 
            IOException
   {
      // TODO: For Miro: Replace the login page with handshake page
      
      // This is new session, which means that the client visited the site
      // for the first time or the client decide not to join session.
      // In both cases redirect client to the login page. The login page
      // should handle scenario if the client has already logged in to another
      // node of a cluster if running in clustered environment and now he is
      // for the first time accessing this node.
      // Store the requested URL in the session object so that after
      // login user can continue to the requested page

      // This can be request which was routed to a new server
      // and therefore the user may have logged in somewhere 
      // else as well. So lets figure out if this is really
      // a brand new session or if it is an attach, but we let the login
      // servlet do it and don't worry about it. If it is an attach then  
      // in the login servlet will immidiately redirect us back.
      // This way also the HTTP session will be established
      saveLoginRedirect(hsSession, WebUtils.getFullRequestURL(hsrqRequest));

      // We cannot use RequestDispatcher because browser would cache this
      // login page as the requested page
      redirectToLogin(hsrqRequest, hsrpResponse);
   }

   /**
    * Verify, if user has already logged into this session.
    *
    * @param  hsSession - HTTP session object
    * @param  hsrqRequest - the servlet request.
    * @param  hsrpResponse - the servlet response.
    * @return Principal - object representing user's credentials or null,
    *                      if user is not logged in yet 
    * @throws ServletException - problem has occurred while processing request
    * @throws IOException - problem has occurred while processing request
    */
   protected Principal verifyLogin(
      HttpSession         hsSession,
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException,
            IOException
   {
      Principal loggedUser;      
      
      loggedUser = WebSessionUtils.getLoggedInUserInfo(hsSession);

      if ((loggedUser != null) && (m_sessionValidator != null))
      {
         // We thing the user is logged in and we have configured session
         // validator, therefore check if the user is really logged in because 
         // it may have been logged out meanwhile
         String strSessionGenCode;
         
         strSessionGenCode = WebSessionUtils.getSessionId(hsSession);
         try
         {
            if (!m_sessionValidator.checkSession(strSessionGenCode))
            {
               // Not logged in anymore
               loggedUser = null;
               saveLoginRedirect(hsSession, WebUtils.getFullRequestURL(hsrqRequest));
               redirectToLogin(hsrqRequest, hsrpResponse);
            }
         }
         catch (OSSException ossExc)
         {
            throw new ServletException("Unexpected exception.", ossExc);
         }
      }
      
      return loggedUser;
   }

   /**
    * Get the URL to which user should be redirected after he is successfully
    * logged into the system.
    *
    * @param hsSession  - HTTP session object
    * @param hsrqRequest - the servlet request
    * @return String - URL to redirect user after login, null if none exists
    */
   protected String getLoginRedirect(
      HttpSession        hsSession,
      HttpServletRequest hsrqRequest
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert hsSession != null : "Session cannot be null here";
      }

      String strLoginRedirect = null;
      
      strLoginRedirect = (String)hsSession.getAttribute(LOGIN_FORWARD_SESSION_PARAM);
      s_logger.finest("Retrieved login redirect to " + strLoginRedirect);
      
      if (strLoginRedirect != null)
      {
         // there has to be switched from https -> http or from http -> https
         // so call particular method that will return toggled URL
         strLoginRedirect = WebUtils.toggleSecure(hsrqRequest, 
                                                  strLoginRedirect, 
                                                  isApplicationSecure());
      }

      return strLoginRedirect;
   }

   /**
    * Save the URL to which user should be redirected after he is successfully
    * logged in to the system. If there already exist redirection URL and user
    * has not been redirected there yet, this request will be ignored
    *
    * @param  hsSession  - HTTP session object
    * @param  strFullRedirectURL - URL to redirect user after login
    * @return boolean - true if URL was set, false if it was ignored
    */
   protected boolean saveLoginRedirect(
      HttpSession hsSession,
      String      strFullRedirectURL
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert hsSession != null : "Session cannot be null here";
      }

      boolean bReturn = false;

      if (hsSession.getAttribute(LOGIN_FORWARD_SESSION_PARAM) == null)
      {
         // No previous value exists
         hsSession.setAttribute(LOGIN_FORWARD_SESSION_PARAM, strFullRedirectURL);
         s_logger.finest("Saved login redirect " + strFullRedirectURL);
         bReturn = true;
      }

      return bReturn;
   }

   /**
    * Reset the URL to which user should be redirected after he is successfully
    * logge to the system to uninitialized value.
    *
    * @param  hsSession  - HTTP session object
    */
   protected void resetLoginRedirect(
      HttpSession hsSession
   )
   {
      if (GlobalConstants.ERROR_CHECKING)
      {
         assert hsSession != null : "Session cannot be null here";
      }

      hsSession.removeAttribute(LOGIN_FORWARD_SESSION_PARAM);
      s_logger.finest("Reseted login redirect");
   }

   /**
    * Redirect client to the login page. This function call has to be the last
    * thing done in response to client's request.
    *
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - problems redirecting to login
    * @throws IOException - problems redirecting to login
    */
   protected void redirectToLogin(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException,
            IOException
   {
      // We cannot use RequestDispatcher because browser would cache this
      // login page as the requested page
      s_logger.finest("Redirecting to login " + hsrqRequest.getContextPath() 
                      + m_strLoginURL);
      redirect(m_strLoginURL, hsrqRequest, hsrpResponse);
   }

   /**
    * Redirect client to the handshake. This function call has to be the 
    * last thing done in response to client's request.
    *
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - problems redirecting to login
    * @throws IOException - problems redirecting to login
    */
   protected void redirectToHandshake(
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException,
            IOException
   {
      // We cannot use RequestDispatcher because browser would cache this
      // login page as the requested page
      s_logger.finest("Redirecting to handshake " + hsrqRequest.getContextPath() 
                      + m_strLoginURL);
      redirect(m_strHandshakeURL, hsrqRequest, hsrpResponse);
   }

   /**
    * Redirect client to another page propagating the internal session ID if any.
    * This function call has to be the  last thing done in response to client's 
    * request.
    *
    * @param strUrl - part of the URL used for constructing final URL 
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @throws ServletException - problems redirecting to login
    * @throws IOException - problems redirecting to login
    */
   protected void redirect(
      String              strUrl,
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse
   ) throws ServletException,
            IOException
   {
      // We cannot use RequestDispatcher because browser would cache this
      // page as the requested page

      // This can be request to attach when user has logged in on some other 
      // server and now we just need to verify that he session exists here
      // If the URL contains the session information we need to send it to login
      // and let the login decide if it lets the user pass as if it was logged in
      // or if it will require from user to login again
      String       strAttachSessionId;
      StringBuffer sbUrl = new StringBuffer();

      sbUrl.append(hsrqRequest.getContextPath());
      sbUrl.append(strUrl);
      if (sbUrl.indexOf(ATTACH_INTERNAL_SESSION_ID_URL_PARAM) == -1)
      {
         strAttachSessionId = hsrqRequest.getParameter(
                                 ATTACH_INTERNAL_SESSION_ID_URL_PARAM);
         if ((strAttachSessionId != null) && (strAttachSessionId.length() > 0))
         {                                               
            // The session parameter is not there yet
            if (sbUrl.indexOf("?") == -1)
            {
               // There is no query string yet
               sbUrl.append("?");
            }
            else
            {
               // There is already a query string so just add another parameter
               sbUrl.append("&");
            }
            sbUrl.append(ATTACH_INTERNAL_SESSION_ID_URL_PARAM);
            sbUrl.append("=");
            sbUrl.append(strAttachSessionId);
         }
      }

      hsrpResponse.sendRedirect(sbUrl.toString());
   }

   /**
    * This method gives derived servlets execute common logic which needs to be 
    * executed for each request. It is executed right before the service itself
    * once handshake was established (if required), login was verified (if 
    * required) and identity of user if one is logged in was established.
    * It can also be called if login is required and we are redirecting to the 
    * login page. 
    * 
    * @param hsSession  - HTTP session object
    * @param hsrqRequest - the servlet request.
    * @param hsrpResponse - the servlet response.
    * @param bLoginVerified - true if login was verified or it is not required, 
    *                         false if we are redirecting to the login page
    * @throws ServletException - problems redirecting to login
    * @throws IOException - problems redirecting to login
    */
   protected void preservice(
      HttpSession         hsSession,
      HttpServletRequest  hsrqRequest,
      HttpServletResponse hsrpResponse,
      boolean             bLoginVerified
   ) throws ServletException,
            IOException
   {
      // Nothing to do for now
   }

   /**
    * Return flag if request should be secure. This method returns application
    * secure flag. Method should be overwtitten within LoginServlet and 
    * will return flag for login page.
    * 
    * @return boolean - true = request should be secure
    *                 - false = request should not be secure
    */
   protected boolean shouldRequestBeSecure(
   )
   {
      return isApplicationSecure();
   }

   /**
    * Return true if application is running as secure (SSL)
    * 
    * @return boolean - true = application is secure (using HTTPS)
    *                 - false = application is not secure (using HTTP)
    */
   protected boolean isApplicationSecure(
   )
   {
      return m_bApplicationSecure;
   }
   
   /**
    * Return true if login is running as secure (SSL)
    * 
    * @return boolean - true = login is secure (using HTTPS)
    *                 - false = login is not secure (using HTTP)
    */
   protected boolean isLoginSecure(
   )
   {
      return m_bLoginSecure;
   }
}
