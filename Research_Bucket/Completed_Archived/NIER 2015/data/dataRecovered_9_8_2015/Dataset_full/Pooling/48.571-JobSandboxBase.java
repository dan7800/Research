/*
 *  Generator-Id: GeneratorEntityObjectBaseJava1.java,v 1.71 2007/07/22 14:30:32 holivier Exp 
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
package org.ofbiz.common.schedule.generated;

import java.util.Map;
import java.util.Locale;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.common.ValueNullRuntimeException;
import java.util.Date;
import java.sql.Timestamp;


import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.common.schedule.developed.RecurrenceInfo;
import org.ofbiz.common.schedule.developed.RecurrenceInfoServices;
import org.ofbiz.common.schedule.developed.JobSandbox;
import org.ofbiz.common.schedule.developed.JobSandboxServices;
import org.ofbiz.common.schedule.developed.RuntimeData;
import org.ofbiz.common.schedule.developed.RuntimeDataServices;


public class JobSandboxBase extends org.neogia.PersistantObject {

    private static final long serialVersionUID = 1L;

    public static final String module = JobSandboxBase.class.getName();
    public static final String resource = "CommonUiLabels";

    /** name of the entity used to store instances of this class in database */
    public static final String ENTITY_NAME = "JobSandbox";

    public String getJobId() {
        return ((String) get("jobId"));
    }

    public String getJobName() {
        return ((String) get("jobName"));
    }
    
    public void setJobName(String _jobName) {
        if (_jobName != null) {
            set("jobName", _jobName);
        } else if (get("jobName") != null) {
            set("jobName", null);
        }
    }

    public java.util.Date getRunTime() {
        return ((Timestamp) get("runTime"));
    }
    
    public void setRunTime(java.util.Date _runTime) {
        if (_runTime != null) {
            set("runTime", _runTime);
        } else if (get("runTime") != null) {
            set("runTime", null);
        }
    }

    public String getPoolId() {
        return ((String) get("poolId"));
    }
    
    public void setPoolId(String _poolId) {
        if (_poolId != null) {
            set("poolId", _poolId);
        } else if (get("poolId") != null) {
            set("poolId", null);
        }
    }

    public String getStatusId() {
        return ((String) get("statusId"));
    }
    
    public void setStatusId(String _statusId) {
        if (_statusId != null) {
            set("statusId", _statusId);
        } else if (get("statusId") != null) {
            set("statusId", null);
        }
    }

    public String getServiceName() {
        return ((String) get("serviceName"));
    }
    
    public void setServiceName(String _serviceName) {
        if (_serviceName != null) {
            set("serviceName", _serviceName);
        } else if (get("serviceName") != null) {
            set("serviceName", null);
        }
    }

    public String getLoaderName() {
        return ((String) get("loaderName"));
    }
    
    public void setLoaderName(String _loaderName) {
        if (_loaderName != null) {
            set("loaderName", _loaderName);
        } else if (get("loaderName") != null) {
            set("loaderName", null);
        }
    }

    public boolean maxRetryNotEmpty() {
        return get("maxRetry") != null ? true : false;
    }

    public long getMaxRetry() {
        Long maxRetry = (Long) get("maxRetry");
        if (maxRetry != null) {
            return ((Long) maxRetry).longValue();
        } else {
            throw new ValueNullRuntimeException("In jobSandbox, attribute maxRetry is null");
        }
    }

    /**
     * return getMaxRetry() if maxRetryNotEmpty() otherwise return defaultValue.
     * @param defaultValue
     */
    public long getMaxRetry(long defaultValue) {
        if (maxRetryNotEmpty()) {
            return getMaxRetry();
        } else {
            return defaultValue;
        }
    }

    public void setMaxRetry2Null() {
        set("maxRetry", null);
    }

    public void setMaxRetry(long _maxRetry) {
        set("maxRetry", new Long(_maxRetry));
    }

    public String getRunAsUser() {
        return ((String) get("runAsUser"));
    }
    
    public void setRunAsUser(String _runAsUser) {
        if (_runAsUser != null) {
            set("runAsUser", _runAsUser);
        } else if (get("runAsUser") != null) {
            set("runAsUser", null);
        }
    }

    public String getRunByInstanceId() {
        return ((String) get("runByInstanceId"));
    }
    
    public void setRunByInstanceId(String _runByInstanceId) {
        if (_runByInstanceId != null) {
            set("runByInstanceId", _runByInstanceId);
        } else if (get("runByInstanceId") != null) {
            set("runByInstanceId", null);
        }
    }

    public java.util.Date getStartDateTime() {
        return ((Timestamp) get("startDateTime"));
    }
    
    public void setStartDateTime(java.util.Date _startDateTime) {
        if (_startDateTime != null) {
            set("startDateTime", _startDateTime);
        } else if (get("startDateTime") != null) {
            set("startDateTime", null);
        }
    }

    public java.util.Date getFinishDateTime() {
        return ((Timestamp) get("finishDateTime"));
    }
    
    public void setFinishDateTime(java.util.Date _finishDateTime) {
        if (_finishDateTime != null) {
            set("finishDateTime", _finishDateTime);
        } else if (get("finishDateTime") != null) {
            set("finishDateTime", null);
        }
    }

    public java.util.Date getCancelDateTime() {
        return ((Timestamp) get("cancelDateTime"));
    }
    
    public void setCancelDateTime(java.util.Date _cancelDateTime) {
        if (_cancelDateTime != null) {
            set("cancelDateTime", _cancelDateTime);
        } else if (get("cancelDateTime") != null) {
            set("cancelDateTime", null);
        }
    }
    
    public RecurrenceInfo getRecurrenceInfoCache() {
        try {
            GenericValue recurrenceInfo = getRelatedOneCache("RecurrenceInfo");
            try {
                return (RecurrenceInfo) recurrenceInfo;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + recurrenceInfo.getClass().getName() + "] returned by getRelatedOneCache doesn't match the expected type [RecurrenceInfo]. Check that a factory is declared in definition of [RecurrenceInfo] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOneCache in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }
                            
    /**
     * @deprecated use getRecurrenceInfoCache() 
     * or getRecurrenceInfoWC() methods instead
     *
     */
    public RecurrenceInfo getRecurrenceInfo() {
        try {
            GenericValue recurrenceInfo = getRelatedOneCache("RecurrenceInfo");
            try {
                return (RecurrenceInfo) recurrenceInfo;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + recurrenceInfo.getClass().getName() + "] returned by getRelatedOneCache doesn't match the expected type [RecurrenceInfo]. Check that a factory is declared in definition of [RecurrenceInfo] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOneCache in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }
    
    public RecurrenceInfo getRecurrenceInfoWC() {
        try {
            GenericValue recurrenceInfo = getRelatedOne("RecurrenceInfo");
            try {
                return (RecurrenceInfo) recurrenceInfo;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + recurrenceInfo.getClass().getName() + "] returned by getRelatedOne doesn't match the expected type [RecurrenceInfo]. Check that a factory is declared in definition of [RecurrenceInfo] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOne in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }

    public void setRecurrenceInfo(RecurrenceInfo _recurrenceInfo) {
            if (_recurrenceInfo != null) {
                put("recurrenceInfoId", _recurrenceInfo.getRecurrenceInfoId() );
            } else {
               if (get("recurrenceInfoId") != null) {
                    set("recurrenceInfoId", null);
                }
            }
    }
    
    public void setRecurrenceInfo(String _recurrenceInfoId) {
            if (_recurrenceInfoId != null) {
                set("recurrenceInfoId", _recurrenceInfoId);
            } else if (get("recurrenceInfoId") != null) {
                set("recurrenceInfoId", null);
            }
    }


    public JobSandbox getParentJobId() {
        try {
            GenericValue parentJobId = getRelatedOne("parentJobIdJobSandbox");
            try {
                return (JobSandbox) parentJobId;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + parentJobId.getClass().getName() + "] returned by getRelatedOne doesn't match the expected type [JobSandbox]. Check that a factory is declared in definition of [JobSandbox] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOne in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }
    
    public JobSandbox getParentJobIdCache() {
        try {
            GenericValue parentJobId = getRelatedOneCache("parentJobIdJobSandbox");
            try {
                return (JobSandbox) parentJobId;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + parentJobId.getClass().getName() + "] returned by getRelatedOneCache doesn't match the expected type [JobSandbox]. Check that a factory is declared in definition of [JobSandbox] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOneCache in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }

    public void setParentJobId(JobSandbox _jobSandbox) {
            if (_jobSandbox != null) {
                put("parentJobIdJobId", _jobSandbox.getJobId() );
            } else {
               if (get("parentJobIdJobId") != null) {
                    set("parentJobIdJobId", null);
                }
            }
    }
    
    public void setParentJobId(String _jobId) {
            if (_jobId != null) {
                set("parentJobIdJobId", _jobId);
            } else if (get("parentJobIdJobId") != null) {
                set("parentJobIdJobId", null);
            }
    }


    public JobSandbox getPreviousJobId() {
        try {
            GenericValue previousJobId = getRelatedOne("previousJobIdJobSandbox");
            try {
                return (JobSandbox) previousJobId;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + previousJobId.getClass().getName() + "] returned by getRelatedOne doesn't match the expected type [JobSandbox]. Check that a factory is declared in definition of [JobSandbox] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOne in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }
    
    public JobSandbox getPreviousJobIdCache() {
        try {
            GenericValue previousJobId = getRelatedOneCache("previousJobIdJobSandbox");
            try {
                return (JobSandbox) previousJobId;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + previousJobId.getClass().getName() + "] returned by getRelatedOneCache doesn't match the expected type [JobSandbox]. Check that a factory is declared in definition of [JobSandbox] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOneCache in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }

    public void setPreviousJobId(JobSandbox _jobSandbox) {
            if (_jobSandbox != null) {
                put("previousJobIdJobId", _jobSandbox.getJobId() );
            } else {
               if (get("previousJobIdJobId") != null) {
                    set("previousJobIdJobId", null);
                }
            }
    }
    
    public void setPreviousJobId(String _jobId) {
            if (_jobId != null) {
                set("previousJobIdJobId", _jobId);
            } else if (get("previousJobIdJobId") != null) {
                set("previousJobIdJobId", null);
            }
    }


    public RuntimeData getRuntimeData() {
        try {
            GenericValue runtimeData = getRelatedOne("RuntimeData");
            try {
                return (RuntimeData) runtimeData;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + runtimeData.getClass().getName() + "] returned by getRelatedOne doesn't match the expected type [RuntimeData]. Check that a factory is declared in definition of [RuntimeData] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOne in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }
    
    public RuntimeData getRuntimeDataCache() {
        try {
            GenericValue runtimeData = getRelatedOneCache("RuntimeData");
            try {
                return (RuntimeData) runtimeData;
            } catch (ClassCastException ex) {
                if (Debug.errorOn()) Debug.logError("The value [" + runtimeData.getClass().getName() + "] returned by getRelatedOneCache doesn't match the expected type [RuntimeData]. Check that a factory is declared in definition of [RuntimeData] Entity", module);
            }
        } catch (GenericEntityException e) {
            Debug.logError("Error getRelatedOneCache in JobSandbox:" + e.getMessage(),  module);
        }
        return null;
    }

    public void setRuntimeData(RuntimeData _runtimeData) {
            if (_runtimeData != null) {
                put("runtimeDataRuntimeDataId", _runtimeData.getRuntimeDataId() );
            } else {
               if (get("runtimeDataRuntimeDataId") != null) {
                    set("runtimeDataRuntimeDataId", null);
                }
            }
    }
    
    public void setRuntimeData(String _runtimeDataId) {
            if (_runtimeDataId != null) {
                set("runtimeDataRuntimeDataId", _runtimeDataId);
            } else if (get("runtimeDataRuntimeDataId") != null) {
                set("runtimeDataRuntimeDataId", null);
            }
    }

    /**
     *  For each attribute test if the context has a value for it and
     *    make the standard check parametered in UML diagram
     *    if it's ok, update the object with the context value
     */
    public Map checkUpdateFromContext(String action, Map context) {
        GenericDelegator delegator = getDelegator();
        Locale locale = (Locale) context.get("locale");

        if (context.containsKey("jobName")) {
            String jobName = (String) context.get("jobName");
            this.setJobName(jobName);
        }

        if (context.containsKey("runTime")) {
            Timestamp runTime = (Timestamp) context.get("runTime");
            this.setRunTime(runTime);
        }

        if (context.containsKey("poolId")) {
            String poolId = (String) context.get("poolId");
            this.setPoolId(poolId);
        }

        if (context.containsKey("statusId")) {
            String statusId = (String) context.get("statusId");
            this.setStatusId(statusId);
        }

        if (context.containsKey("serviceName")) {
            String serviceName = (String) context.get("serviceName");
            this.setServiceName(serviceName);
        }

        if (context.containsKey("loaderName")) {
            String loaderName = (String) context.get("loaderName");
            this.setLoaderName(loaderName);
        }

        if (context.containsKey("maxRetry")) {
            Long maxRetry = (Long) context.get("maxRetry");
            if (maxRetry != null) {
                this.setMaxRetry(maxRetry.longValue());
            } else if (this.maxRetryNotEmpty()) {
                this.setMaxRetry2Null();
            }
        }

        if (context.containsKey("runAsUser")) {
            String runAsUser = (String) context.get("runAsUser");
            this.setRunAsUser(runAsUser);
        }

        if (context.containsKey("runByInstanceId")) {
            String runByInstanceId = (String) context.get("runByInstanceId");
            this.setRunByInstanceId(runByInstanceId);
        }

        if (context.containsKey("startDateTime")) {
            Timestamp startDateTime = (Timestamp) context.get("startDateTime");
            this.setStartDateTime(startDateTime);
        }

        if (context.containsKey("finishDateTime")) {
            Timestamp finishDateTime = (Timestamp) context.get("finishDateTime");
            this.setFinishDateTime(finishDateTime);
        }

        if (context.containsKey("cancelDateTime")) {
            Timestamp cancelDateTime = (Timestamp) context.get("cancelDateTime");
            this.setCancelDateTime(cancelDateTime);
        }

        if (context.containsKey("recurrenceInfoId") ) {
            String recurrenceInfoId = (String) context.get("recurrenceInfoId");
            if (recurrenceInfoId != null) {
                RecurrenceInfo recurrenceInfoO = RecurrenceInfoServices.findByPrimaryKeyCache(delegator, recurrenceInfoId);
                if (recurrenceInfoO != null) {
                    this.setRecurrenceInfo(recurrenceInfoO);
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonRecurrenceInfoNotExist", locale));
                }
            }
            else {
                this.setRecurrenceInfo(recurrenceInfoId );
            }
        }

        if (context.containsKey("parentJobIdJobId") ) {
            String parentJobIdJobId = (String) context.get("parentJobIdJobId");
            if (parentJobIdJobId != null) {
                JobSandbox parentJobIdO = JobSandboxServices.findByPrimaryKeyCache(delegator, parentJobIdJobId);
                if (parentJobIdO != null) {
                    this.setParentJobId(parentJobIdO);
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonParentJobIdNotExist", locale));
                }
            }
            else {
                this.setParentJobId(parentJobIdJobId );
            }
        }

        if (context.containsKey("previousJobIdJobId") ) {
            String previousJobIdJobId = (String) context.get("previousJobIdJobId");
            if (previousJobIdJobId != null) {
                JobSandbox previousJobIdO = JobSandboxServices.findByPrimaryKeyCache(delegator, previousJobIdJobId);
                if (previousJobIdO != null) {
                    this.setPreviousJobId(previousJobIdO);
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonPreviousJobIdNotExist", locale));
                }
            }
            else {
                this.setPreviousJobId(previousJobIdJobId );
            }
        }

        if (context.containsKey("runtimeDataRuntimeDataId") ) {
            String runtimeDataRuntimeDataId = (String) context.get("runtimeDataRuntimeDataId");
            if (runtimeDataRuntimeDataId != null) {
                RuntimeData runtimeDataO = RuntimeDataServices.findByPrimaryKeyCache(delegator, runtimeDataRuntimeDataId);
                if (runtimeDataO != null) {
                    this.setRuntimeData(runtimeDataO);
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonRuntimeDataNotExist", locale));
                }
            }
            else {
                this.setRuntimeData(runtimeDataRuntimeDataId );
            }
        }

        return null;
    }

    /**
     *  Prepare a map useable for the FormWrapper, put all the attribute execpt PK and hidden attribute
     *    for association put PK and the toString method result
     *    before put it test if the parameter has a value for it and
     */
    public void toFormMap(Map formData, Locale locale) {
        if (formData.get("jobName") == null) {
            formData.put("jobName", get("jobName"));
        }
        if (formData.get("runTime") == null) {
            formData.put("runTime", get("runTime"));
        }
        if (formData.get("poolId") == null) {
            formData.put("poolId", get("poolId"));
        }
        if (formData.get("statusId") == null) {
            formData.put("statusId", get("statusId"));
        }
        if (formData.get("serviceName") == null) {
            formData.put("serviceName", get("serviceName"));
        }
        if (formData.get("loaderName") == null) {
            formData.put("loaderName", get("loaderName"));
        }
        if (formData.get("maxRetry") == null) {
            formData.put("maxRetry", get("maxRetry"));
        }
        if (formData.get("runAsUser") == null) {
            formData.put("runAsUser", get("runAsUser"));
        }
        if (formData.get("runByInstanceId") == null) {
            formData.put("runByInstanceId", get("runByInstanceId"));
        }
        if (formData.get("startDateTime") == null) {
            formData.put("startDateTime", get("startDateTime"));
        }
        if (formData.get("finishDateTime") == null) {
            formData.put("finishDateTime", get("finishDateTime"));
        }
        if (formData.get("cancelDateTime") == null) {
            formData.put("cancelDateTime", get("cancelDateTime"));
        }

        RecurrenceInfo recurrenceInfo = this.getRecurrenceInfo();
        if (recurrenceInfo != null) {
            if (formData.get("recurrenceInfoId") == null) {
                formData.put("recurrenceInfoId", get("recurrenceInfoId"));
            }
            formData.put("recurrenceInfo2String", recurrenceInfo.toDisplayString());
        }

        JobSandbox parentJobId = this.getParentJobIdCache();
        if (parentJobId != null) {
                                if (formData.get("parentJobIdJobId") == null) {
                formData.put("parentJobIdJobId", get("parentJobIdJobId"));
            }
            formData.put("parentJobId2String", parentJobId.toDisplayString());
        }

        JobSandbox previousJobId = this.getPreviousJobIdCache();
        if (previousJobId != null) {
                                if (formData.get("previousJobIdJobId") == null) {
                formData.put("previousJobIdJobId", get("previousJobIdJobId"));
            }
            formData.put("previousJobId2String", previousJobId.toDisplayString());
        }

        RuntimeData runtimeData = this.getRuntimeDataCache();
        if (runtimeData != null) {
                                if (formData.get("runtimeDataRuntimeDataId") == null) {
                formData.put("runtimeDataRuntimeDataId", get("runtimeDataRuntimeDataId"));
            }
            formData.put("runtimeData2String", runtimeData.toDisplayString());
        }
    }

    /**
     * Build a map representing the object for reporting engine
     */
    public Map toReportMap(Locale locale) {

        Map record = UtilMisc.toMap(new Object[0]);
        record.put("CommonJobSandbox", UtilProperties.getMessage(resource, "CommonJobSandbox", locale));

        record.put("CommonjobId", UtilProperties.getMessage(resource, "CommonJobId", locale));

        record.put("jobId", this.getJobId());

        record.put("CommonjobName", UtilProperties.getMessage(resource, "CommonJobName", locale));

        if (this.getJobName() != null) {
            record.put("jobName", this.getJobName());
        } else {
            record.put("jobName", null);
        }

        record.put("CommonrunTime", UtilProperties.getMessage(resource, "CommonRunTime", locale));

        if (this.getRunTime() != null) {
            record.put("runTime", this.getRunTime());
        } else {
            record.put("runTime", null);
        }

        record.put("CommonpoolId", UtilProperties.getMessage(resource, "CommonPoolId", locale));

        if (this.getPoolId() != null) {
            record.put("poolId", this.getPoolId());
        } else {
            record.put("poolId", null);
        }

        record.put("CommonstatusId", UtilProperties.getMessage(resource, "CommonStatusId", locale));

        if (this.getStatusId() != null) {
            record.put("statusId", this.getStatusId());
        } else {
            record.put("statusId", null);
        }

        record.put("CommonserviceName", UtilProperties.getMessage(resource, "CommonServiceName", locale));

        if (this.getServiceName() != null) {
            record.put("serviceName", this.getServiceName());
        } else {
            record.put("serviceName", null);
        }

        record.put("CommonloaderName", UtilProperties.getMessage(resource, "CommonLoaderName", locale));

        if (this.getLoaderName() != null) {
            record.put("loaderName", this.getLoaderName());
        } else {
            record.put("loaderName", null);
        }

        record.put("CommonmaxRetry", UtilProperties.getMessage(resource, "CommonMaxRetry", locale));

        if (this.maxRetryNotEmpty()) {
             record.put("maxRetry", new Long(this.getMaxRetry()));
        } else {
             record.put("maxRetry", null);
        }

        record.put("CommonrunAsUser", UtilProperties.getMessage(resource, "CommonRunAsUser", locale));

        if (this.getRunAsUser() != null) {
            record.put("runAsUser", this.getRunAsUser());
        } else {
            record.put("runAsUser", null);
        }

        record.put("CommonrunByInstanceId", UtilProperties.getMessage(resource, "CommonRunByInstanceId", locale));

        if (this.getRunByInstanceId() != null) {
            record.put("runByInstanceId", this.getRunByInstanceId());
        } else {
            record.put("runByInstanceId", null);
        }

        record.put("CommonstartDateTime", UtilProperties.getMessage(resource, "CommonStartDateTime", locale));

        if (this.getStartDateTime() != null) {
            record.put("startDateTime", this.getStartDateTime());
        } else {
            record.put("startDateTime", null);
        }

        record.put("CommonfinishDateTime", UtilProperties.getMessage(resource, "CommonFinishDateTime", locale));

        if (this.getFinishDateTime() != null) {
            record.put("finishDateTime", this.getFinishDateTime());
        } else {
            record.put("finishDateTime", null);
        }

        record.put("CommoncancelDateTime", UtilProperties.getMessage(resource, "CommonCancelDateTime", locale));

        if (this.getCancelDateTime() != null) {
            record.put("cancelDateTime", this.getCancelDateTime());
        } else {
            record.put("cancelDateTime", null);
        }

        record.put("CommonrecurrenceInfo", UtilProperties.getMessage(resource, "CommonRecurrenceInfo", locale));

        record.put("recurrenceInfo", null);
        if (this.getRecurrenceInfo() != null) {
            if (this.getRecurrenceInfo().toString() != null) {
                record.put("recurrenceInfo", this.getRecurrenceInfo().toString());
            }
        }

        record.put("CommonparentJobId", UtilProperties.getMessage(resource, "CommonParentJobId", locale));

        record.put("parentJobId", null);
        if (this.getParentJobId() != null) {
            if (this.getParentJobId().toString() != null) {
                record.put("parentJobId", this.getParentJobId().toString());
            }
        }

        record.put("CommonpreviousJobId", UtilProperties.getMessage(resource, "CommonPreviousJobId", locale));

        record.put("previousJobId", null);
        if (this.getPreviousJobId() != null) {
            if (this.getPreviousJobId().toString() != null) {
                record.put("previousJobId", this.getPreviousJobId().toString());
            }
        }

        record.put("CommonruntimeData", UtilProperties.getMessage(resource, "CommonRuntimeData", locale));

        record.put("runtimeData", null);
        if (this.getRuntimeData() != null) {
            if (this.getRuntimeData().toString() != null) {
                record.put("runtimeData", this.getRuntimeData().toString());
            }
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
            Debug.logError("create or strore in JobSandbox :" + e.getMessage(),  module);
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
            Debug.logError("remove in JobSandbox :" + e.getMessage(),  module);
        }
        return false;
    }
    public Map afterRemove(Map context) {
        return null;
    }


    public String toDisplayString() {

        return "{"+"JobId: " + getJobId()+"}";

    }

}
