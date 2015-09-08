/*
 * Copyright 2005-8 Pi4 Technologies Ltd
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
 * 6 Feb 2008 : Initial version created by gary
 */
package org.pi4soa.common.validation.eclipse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.jobs.Job;
import org.pi4soa.common.model.*;

public class ValidationManager {

	/**
	 * This method validates the supplied model and resource.
	 * 
	 * @param model The model
	 * @param res The resource
	 */
	public static void validate(Model model, IResource res) {
		
		try {				
			// Create the validation job
			ValidationJob job=new ValidationJob(model,
					res, m_listeners);
			job.setPriority(Job.DECORATE);
			job.schedule();
				
		} catch(Exception e) {
			logger.log(Level.SEVERE,
					"Failed to schedule validation of model", e);
		}
	}
	
	/**
	 * This method adds a listener to the validation manager.
	 * 
	 * @param l The listener
	 */
	public static void addValidationManagerListener(ValidationManagerListener l) {
		synchronized(m_listeners) {
			if (m_listeners.contains(l) == false) {
				m_listeners.add(l);
			}
		}
	}
	
	/**
	 * This method removes a listener to the validation manager.
	 * 
	 * @param l The listener
	 */
	public static void removeValidationManagerListener(ValidationManagerListener l) {
		synchronized(m_listeners) {
			m_listeners.remove(l);
		}
	}
	
    private static Logger logger = Logger.getLogger("org.pi4soa.common.validation.eclipse");
    
    private static java.util.List m_listeners=new java.util.Vector();
}
