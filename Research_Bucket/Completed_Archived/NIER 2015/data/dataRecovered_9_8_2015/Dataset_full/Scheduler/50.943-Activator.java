package org.pi4soa.scenario.eclipse;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.pi4soa.common.eclipse.BundleUtil;
import org.pi4soa.common.model.ModelListener;
import org.pi4soa.common.resource.ResourceLocator;
import org.pi4soa.common.resource.eclipse.EclipseResourceProperties;
import org.pi4soa.common.util.EMFUtil;
import org.pi4soa.common.validation.Validator;
import org.pi4soa.scenario.*;
import org.pi4soa.scenario.validation.ScenarioValidationRuleSet;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.pi4soa.scenario";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		initialize();
	}

	/**
	 * This method initializes the service test plugin.
	 *
	 */
	protected void initialize() {
		
		try {
			ResourceBundle res=ResourceBundle.getBundle(ScenarioManager.SERVICE_TEST_MODULE);
			
			ResourceLocator.setResourceBundle(
					ScenarioManager.SERVICE_TEST_MODULE, res);
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
								
							/* Modification related to sourceforge
							 * bug "[ 1676704 ] encoding change not working"
							 * attempting to resave the file when the encoding
							 * is changed. Issue is that it needs to be done
							 * is separate thread, otherwise gets a resource
							 * locked exception, but if the file happens to be
							 * dirty when the encoding is changed, it then
							 * reloads the pre-changed version, losing the
							 * changes.
							 * 
							} else if (isEncodingChange(res, delta) &&
									res instanceof IFile) {
								final IFile file=(IFile)res;
								
								org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										
										// Load and save scenario to change the encoding
										try {
											IProgressMonitor progressMonitor= new NullProgressMonitor();
											
											org.pi4soa.scenario.Scenario scenario=
												org.pi4soa.scenario.ScenarioManager.load(file.getLocation().toOSString());
											
								        	ByteArrayOutputStream baos=new ByteArrayOutputStream();
								        	
								            ScenarioManager.save(scenario, baos, file.getCharset(true));
								            //org.pi4soa.scenario.ScenarioManager.save(m_Scenario,
								            //		baos, file.getCharset(true));
								            
								            baos.close();
								            
								            ByteArrayInputStream bais=
								            		new ByteArrayInputStream(baos.toByteArray());
								            
								            file.setContents(bais, true, true, progressMonitor);
								            
								            bais.close();
								            
								            progressMonitor.worked(1);
								            file.refreshLocal(
								                IResource.DEPTH_ZERO,
								                new SubProgressMonitor(progressMonitor, 1));
								            progressMonitor.done();

										} catch(Exception e) {
											logError("Failed to update encoding on '"+file.getLocation()+"'", e);
										}
									}
								});
							*/
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
	 * This method validates the supplied resource.
	 * 
	 * @param res The resource
	 */
	protected void validateResource(IResource res) {
		
		try {				
			// Identify the package associated with the
			// changed resource
			Scenario scenario=getScenario(res);
			
			// Create the validation job
			ScenarioValidation job=new ScenarioValidation(scenario,
					res);
			job.setPriority(Job.DECORATE);
			job.schedule();
				
		} catch(Exception e) {
			logger.log(Level.SEVERE,
					"Failed to schedule validation of CDL model", e);
		}
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
				ScenarioDefinitions.isScenarioExtension(
						res.getFileExtension()) &&
				(((delta.getFlags() & IResourceDelta.CONTENT) != 0) ||
				delta.getKind() == IResourceDelta.ADDED)) {
			ret = true;
		}

		return(ret);
	}

	/**
	 * This method determines whether the encoding has changed on the
	 * file.
	 * 
	 * @param res The resource
	 * @param delta The resource delta
	 * @return Whether the encoding has changed
	 */
	protected boolean isEncodingChange(IResource res, IResourceDelta delta) {
		boolean ret=false;

		// Is the resource a CDL file?
		// Are the changes associated with the contents?
		if (res != null && res.getFileExtension() != null &&
				ScenarioDefinitions.isScenarioExtension(
						res.getFileExtension()) &&
				(delta.getFlags() & IResourceDelta.ENCODING) != 0) {
			ret = true;
		}

		return(ret);
	}
	
    /**
     * This method loads a CDL package from a specified
     * resource.
     * 
     * @param res The resource
     * @return The CDL package
     * @throws java.io.IOException Failed to load file
     * @throws CoreException Failed to extract CDL content
     */
    public static Scenario getScenario(IResource res)
				throws java.io.IOException, CoreException {
    	Scenario ret=null;
		
        // Check if the resource is a file
		if (res instanceof IFile) {
			
		    // Load the CDL package from the file contents
			java.io.InputStream is=((IFile)res).getContents();
			
			// Determine a URI for the CDL resource file
			URI uri=URI.createFileURI(((IFile)res).getFullPath().toString());

			// Instantiate the CDL package
			org.pi4soa.cdl.CdlPackage.eINSTANCE.getPackage();
			org.pi4soa.cdl.impl.CdlPackageImpl.init();
			
			final org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl xmi =
				new org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl();
			xmi.setURI(uri);
			xmi.doLoad(is, xmi.getDefaultLoadOptions());
			org.eclipse.emf.common.util.EList list = xmi.getContents();

			// Check that the top level object is a Scenario
			if (list.get(0) instanceof Scenario) {
				ret = (Scenario)list.get(0);
			}
		}
		
		return(ret);
    }

    /*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * This method logs an error against the CDL plugin.
	 * 
	 * @param mesg The error message
	 * @param t The optional exception
	 */
	public static void logError(String mesg, Throwable t) {
		
		if (getDefault() != null) {
			Status status=new Status(IStatus.ERROR,
					SCENARIO_PLUGIN_ID, 0, mesg, t);
			
			getDefault().getLog().log(status);
		}
		
		logger.severe("LOG ERROR: "+mesg+
				(t == null ? "" : ": "+t));
	}
	
    private static Logger logger = Logger.getLogger("org.pi4soa.scenario.eclipse");

    public static final String SCENARIO_PLUGIN_ID="org.pi4soa.scenario";
	private static final String SERVICE_PLUGIN_ID = "org.pi4soa.service";

    static {		
		BundleUtil.registerClasspathEntries(SCENARIO_PLUGIN_ID, false);
		BundleUtil.registerClasspathEntries(SERVICE_PLUGIN_ID, true);
	}
	
    /**
     * This class implements the CDL validation task.
     */
    class ScenarioValidation extends Job implements ModelListener {
    	
    	/**
    	 * This is the constructor for the CDL validation job.
    	 * 
    	 * @param cdlpack The CDL package to be validated.
    	 * @param res The resource associated with the package.
    	 */
    	public ScenarioValidation(Scenario scenario,
    					IResource res) {
    		super("ScenarioValidation");
    		
    		m_scenario = scenario;
    		m_resource = res;
    	}
    	
    	/**
    	 * This method runs the validation task.
    	 * 
    	 * @param mon The progress monitor
    	 * @return The status of the job
    	 */
    	public IStatus run(IProgressMonitor mon) {
    		
			try {				
				EclipseResourceProperties props=
					new EclipseResourceProperties(m_resource,
							m_resource.getProject());
				
				// Update the resource URI, based on the
				// platform specific location of the resource
				org.eclipse.emf.common.util.URI uri=
					org.eclipse.emf.common.util.URI.createFileURI(
							m_resource.getLocation().toOSString());
				m_scenario.eResource().setURI(uri);

				// Validate the package
				Validator.validate(m_scenario, props, this);
				
				org.eclipse.swt.widgets.Display.getDefault().syncExec(new Runnable() {
					public void run() {
						
						// Clear current markers
						try {
							m_resource.deleteMarkers(EValidator.MARKER, true,
									IResource.DEPTH_INFINITE);
							
							// Update the markers
							for (int i=0; i < m_entries.size(); i++) {
								ReportEntry re=(ReportEntry)m_entries.get(i);
								createMarker(re.getSource(),
										re.getMessage(), re.getType(),
										re.getProperties());
							}
						} catch(Exception e) {
							logger.log(Level.SEVERE,
									"Failed to update problems", e);
							Activator.logError("Failed to update problems " +
									"view with validation messages",e);
						}
					}
				});
			} catch(Exception e) {
				
				logger.log(Level.SEVERE,
						"Failed to validate CDL model", e);
				
				Activator.logError("Failed to validate CDL model",e);
			}
			
			return(Status.OK_STATUS);
    	}
    	
        /**
         * This method is used to report information, warning
         * and error messages related to an operation performed
         * on the object model. The source of the message
         * may be an EMF derived object, or a DOM object
         * that is being used to import an object model.
         * 
         * @param source The source object
         * @param mesg The message
         * @param reportType The report type (info, warning
         * 					or error)
         */
        public void report(Object source, String mesg,
                int reportType) {
        	report(source, mesg, reportType, null);
        }
        
        /**
         * This method is used to report information, warning
         * and error messages related to an operation performed
         * on the object model. The source of the message
         * may be an EMF derived object, or a DOM object
         * that is being used to import an object model.
         * 
         * @param source The source object
         * @param mesg The message
         * @param reportType The report type (info, warning
         * 					or error)
         * @param props The optional properties associated with
         * 					the reported issue
         */
        public void report(Object source, String mesg,
                int reportType, java.util.Properties props) {
			m_entries.add(new ReportEntry(source, mesg, reportType, props));
		}
		
		protected void createMarker(Object src, String mesg,
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
				String uristring=EMFUtil.getURI(src);
					
				marker.setAttribute(EValidator.URI_ATTRIBUTE,
							uristring);

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
				
				Activator.logError("Failed to validate Test Scenario model",
						e);						
			}
		}
		
    	private Scenario m_scenario=null; 
    	private IResource m_resource=null;
    	private java.util.Vector m_entries=new java.util.Vector();
    	
    	/**
    	 * This is a simple data container class to hold the
    	 * information reported during validation.
    	 *
    	 */
    	public class ReportEntry {
    		public ReportEntry(Object src, String mesg,
				int type, java.util.Properties props) {
    			m_source = src;
    			m_message = mesg;
    			m_type = type;
    			m_properties = props;
    		}
    		
    		public Object getSource() {
    			return(m_source);
    		}
    		
    		public String getMessage() {
    			return(m_message);
    		}
    		
    		public int getType() {
    			return(m_type);
    		}
    		
    		public java.util.Properties getProperties() {
    			return(m_properties);
    		}
    		
    		private Object m_source=null;
    		private String m_message=null;
    		private int m_type=0;
    		private java.util.Properties m_properties=null;
    	}
    }
}
