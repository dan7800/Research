/*
 * Copyright 2005 Pi4 Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Change History:
 * Jun 27, 2005 : Initial version created by gary
 */
package org.pi4soa.service.eclipse;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;
import org.pi4soa.service.behavior.validation.BaseValidationRuleSet;
import org.pi4soa.common.eclipse.BundleUtil;
import org.pi4soa.common.resource.ResourceLocator;
import org.pi4soa.common.resource.eclipse.EclipseResourceProperties;
import org.pi4soa.common.util.EMFUtil;
import org.pi4soa.common.validation.Validator;
import org.pi4soa.service.ServiceDefinitions;
import org.pi4soa.service.behavior.BehaviorType;
import org.pi4soa.common.model.ModelListener;
import org.pi4soa.service.behavior.ServiceDescription;

/**
 * This class is the plugin implementation for the Service component
 * of the pi4soa tool suite. Its purpose is simply to initialize
 * supporting features when run within the Eclipse environment.
 */
public class ServicePlugin extends Plugin {

	/**
	 * The constructor
	 */
	public ServicePlugin() {
		super();
		// TODO Auto-generated constructor stub
		
		initialize();

		m_instance = this;
	}
	
	protected void initialize() {
		
		// Initialize resources for service related modules
		try {
			ResourceBundle res=ResourceBundle.getBundle("behavior");
			
			ResourceLocator.setResourceBundle(
					"behavior", res);
		} catch(Exception e) {
			// TODO: report error
		}
		
		try {
			ResourceBundle res=ResourceBundle.getBundle("container");
			
			ResourceLocator.setResourceBundle(
					"container", res);
		} catch(Exception e) {
			// TODO: report error
		}
		
		try {
			ResourceBundle res=ResourceBundle.getBundle("endpoint");
			
			ResourceLocator.setResourceBundle(
					"endpoint", res);
		} catch(Exception e) {
			// TODO: report error
		}
		
		try {
			ResourceBundle res=ResourceBundle.getBundle("monitor");
			
			ResourceLocator.setResourceBundle(
					"monitor", res);
		} catch(Exception e) {
			// TODO: report error
		}	
		
		try {
			ResourceBundle res=ResourceBundle.getBundle("session");
			
			ResourceLocator.setResourceBundle(
					"session", res);
		} catch(Exception e) {
			// TODO: report error
			e.printStackTrace();
		}	
		
		// Initialize the resource change listener
		IResourceChangeListener rcl=
					new IResourceChangeListener() {
			
			public void resourceChanged(IResourceChangeEvent evt) {

				try {
					evt.getDelta().accept(new IResourceDeltaVisitor() {
						
				        public boolean visit(IResourceDelta delta) {
				        	boolean ret=true;
				        	IResource res = delta.getResource();
				        	
							// Determine if the change is relevant
							if (isChangeRelevant(res,
										delta)) {
								
								// Validate the resource
								validateResource(res);
							}
							
				        	return(ret);
				        }
				 	});
				} catch(Exception e) {
					logger.log(Level.SEVERE,
						"Failed to process resource change event",
						e);
				}
			}
		};
		
		// Register the resource change listener
		ResourcesPlugin.getWorkspace().addResourceChangeListener(rcl,
				IResourceChangeEvent.POST_CHANGE);		
	}

	/**
	 * This method returns a reference to the CDL plugin
	 * instance.
	 * 
	 * @return The singleton CDL plugin instance
	 */
	public static ServicePlugin instance() {
		return(m_instance);
	}
	
	/**
	 * This method validates the supplied resource.
	 * 
	 * @param res The resource
	 */
	protected void validateResource(IResource res) {
		
		try {				
			// Identify the package associated with the
			// changed resource
			if (res instanceof IFile) {
				ServiceDescription sd=getServiceDescription(res);
			
				// Create the validation job
				SEDLValidation job=new SEDLValidation(sd,
						res);
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE,
					"Failed to schedule validation of SEDL model", e);
		}
	}
	
    /**
     * This method loads a service description from a specified
     * resource.
     * 
     * @param res The resource
     * @return The service description
     * @throws java.io.IOException Failed to load file
     * @throws CoreException Failed to extract content
     */
    public static ServiceDescription getServiceDescription(IResource res)
				throws java.io.IOException, CoreException {
    	ServiceDescription ret=null;
		
        // Check if the resource is a file
		if (res instanceof IFile) {
			
		    // Load the CDL package from the file contents
			java.io.InputStream is=((IFile)res).getContents();
			
			// Determine a URI for the resource file
			URI uri=URI.createFileURI(((IFile)res).getFullPath().toString());

			// Instantiate the service description
			org.pi4soa.service.behavior.BehaviorPackage.eINSTANCE.getServiceDescription();
			org.pi4soa.service.behavior.impl.BehaviorPackageImpl.init();
			
			final org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl xmi =
				new org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl();
			xmi.setURI(uri);
			xmi.doLoad(is, xmi.getDefaultLoadOptions());
			org.eclipse.emf.common.util.EList list = xmi.getContents();

			// Check that the top level object is a Service Description
			if (list.get(0) instanceof ServiceDescription) {
				ret = (ServiceDescription)list.get(0);
			}
		}
		
		return(ret);
    }

    /**
	 * This method determines whether the supplied resource
	 * change event is relevant.
	 * 
	 * @param res The resource
	 * @param deltaFlags The flags
	 * @return Whether the change is relevant
	 */
	protected boolean isChangeRelevant(IResource res, IResourceDelta delta) {
		boolean ret=false;

		// Is the resource a CDL file?
		// Are the changes associated with the contents?
		if (res != null && res.getFileExtension() != null &&
				res.getFileExtension().equals(ServiceDefinitions.SERVICE_ENDPOINT_FILE_EXTENSION) &&
				(((delta.getFlags() & IResourceDelta.CONTENT) != 0) ||
				delta.getKind() == IResourceDelta.ADDED)) {
			ret = true;
		}

		return(ret);
	}

	/**
	 * This method logs an error against the CDL plugin.
	 * 
	 * @param mesg The error message
	 * @param t The optional exception
	 */
	public static void logError(String mesg, Throwable t) {
		
		if (instance() != null) {
			Status status=new Status(IStatus.ERROR,
					ServiceDefinitions.SERVICE_PLUGIN_ID, 0, mesg, t);
			
			instance().getLog().log(status);
		}
		
		logger.severe("LOG ERROR: "+mesg+
				(t == null ? "" : ": "+t));
	}
	
    private static Logger logger = Logger.getLogger("org.pi4soa.service.eclipse");

    private static ServicePlugin m_instance=null;

	private static final String SERVICE_PLUGIN_ID = "org.pi4soa.service";

    static {
		
		BundleUtil.registerClasspathEntries(SERVICE_PLUGIN_ID, true);
	}

    /**
     * This class implements the SEDL validation task.
     */
    class SEDLValidation extends Job {
    	
    	/**
    	 * This is the constructor for the SEDL validation job.
    	 * 
    	 * @param sd The service description to be validated.
    	 * @param res The resource associated with the description.
    	 */
    	public SEDLValidation(ServiceDescription sd,
    					IResource res) {
    		super("SEDLValidation");
    		
    		m_servDesc = sd;
    		m_resource = res;
    	}
    	
    	/**
    	 * This method runs the validation task.
    	 * 
    	 * @param mon The progress monitor
    	 * @return The status of the job
    	 */
    	public IStatus run(IProgressMonitor mon) {
    		
			// Create model listener
			ModelListener listener=new ModelListener() {
				
				public synchronized void report(Object src, String mesg,
						int type) {
					report(src, mesg, type, null);
				}
					
				public synchronized void report(Object src, String mesg,
						int type, java.util.Properties props) {
					
					// Create marker for message
					try {
						IMarker marker=m_resource.createMarker(EValidator.MARKER);
						
						// Initialize the attributes on the marker
						marker.setAttribute(IMarker.MESSAGE, mesg);
						
						if (type == ModelListener.ERROR_TYPE) {
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
						} else if (type == ModelListener.WARNING_TYPE) {
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
						} else if (type == ModelListener.INFORMATION_TYPE) {
							marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
						}
						
						// Identify the URI for the EMF object
						if (src instanceof BehaviorType) {
							String uristring=EMFUtil.getURI(src);
							
							marker.setAttribute(EValidator.URI_ATTRIBUTE,
									uristring);
						}
						
						// Transfer properties
						if (props != null) {
							java.util.Iterator iter=props.keySet().iterator();
							
							while (iter.hasNext()) {
								String prop=(String)iter.next();
								String value=props.getProperty(prop);
								
								marker.setAttribute(prop, value);
							}
						}
					} catch(Exception e) {
						
						// TODO: report error
						e.printStackTrace();
						
						ServicePlugin.logError("Failed to validate CDL model",
								e);						
					}
				}
			};
			
			try {				
				// Clear current markers
				m_resource.deleteMarkers(EValidator.MARKER, true,
						IResource.DEPTH_INFINITE);
				
				EclipseResourceProperties props=
					new EclipseResourceProperties(m_resource,
							m_resource.getProject());

				// Validate the description
				Validator.validate(m_servDesc, props, listener);
				
			} catch(Exception e) {
				
				logger.log(Level.SEVERE,
						"Failed to validate SEDL model", e);
				
				ServicePlugin.logError("Failed to validate SEDL model",e);
			}
			
			return(Status.OK_STATUS);
    	}
    	
    	private ServiceDescription m_servDesc=null; 
    	private IResource m_resource=null;
    }
}
