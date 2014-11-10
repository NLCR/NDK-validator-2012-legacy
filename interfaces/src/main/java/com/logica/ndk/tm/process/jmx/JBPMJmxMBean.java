package com.logica.ndk.tm.process.jmx;


/**
 * Interface of JMX MBean. We can use here only methods with primitive types so there is small difference between this
 * JMX IF and WS IF (JBPMWSFacade).
 * 
 * @author Rudolf Daco
 */
public interface JBPMJmxMBean {
    /**
     * Creates a new process instance (but does not yet start it). The process (definition) that should be used is
     * referenced by the given process id. Parameters can be passed to the process instance (as name-value pairs), and
     * these will be set as variables of the process instance. You should only use this method if you need a reference
     * to the process instance before actually starting it. Otherwise, use startProcess. Created instance can be started
     * by method startProcessInstance or aborted by method endInstance.
     * 
     * @param processId the id of the process that should be started
     * @return id of process instance
     */
    public String createProcessInstance(String processId);

    /**
     * Starts the given process instance (which was created by using createProcesInstance but not yet started). This
     * method can only be called once for each process instance. You should only use this method if you need a reference
     * to the process instance before actually starting it. Otherwise, use startProcess.
     * 
     * @param processId the id of the process instance that needs to be started
     * @return id of started instance
     */
    public String startProcessInstance(long processInstanceId);

    /**
     * Start a new process instance. The process (definition) that should be used is referenced by the given process id.
     * Parameters can be passed to the process instance (as name-value pairs), and these will be set as variables of the
     * process instance.
     * 
     * @param processId the id of the process that should be started
     * @return id of precess that wa started
     */
    public String startProcess(String processId);

    /**
     * Aborts the process instance with the given id. This can be used to abort already started instance but this can be
     * also used to abort instance which was only created but not started.
     * 
     * @param instanceId id of process instance to abort
     * @param initiator name of initiator which requests abort
     * @return only OK message now
     */
    public String endInstance(long instanceId, String initiator);

    /**
     * Return instanceId of all active instances for this process.
     * 
     * @param processId defines id of process to get instances
     * @return status of all instances of this process
     */
    public String activeInstances(String processId);

    /**
     * Signals that an event has occurred. The type parameter defines which type of event and the event parameter can
     * contain additional information related to the event.
     * 
     * @param instanceId id of instance to signal event.
     * @param type type of event to signal
     * @param eventData Data related to the event can be passed using the eventData parameter. If the event node
     * specifies a variable name, this data will be copied to that variable when the event occurs.
     * @return only OK message now
     */
    public String signalEventForInstance(long instanceId, String type, String eventData);

    /**
     * Signals the engine that an event has occurred. The type parameter defines which type of event and the event
     * parameter can contain additional information related to the event. All process instances that are listening to
     * this type of (external) event will be notified. For performance reasons, this type of event signaling should only
     * be used if one process instance should be able to notify other process instances. For internal event within one
     * process instance, use the signalEvent method that also include the processInstanceId of the process instance in
     * question.
     * 
     * @param type the type of event
     * @param eventData Data related to the event can be passed using the eventData parameter. If the event node
     * specifies a variable name, this data will be copied to that variable when the event occurs.
     * @return only OK message now
     */
    public String signalEvent(String type, String eventData);

    /**
     * Return state of process instance with this instanceId.
     * 
     * @param instanceId id of process instance
     * @return process state
     */
    public String state(long instanceId);
    
    /**
     * Resume all not finished processes which was previously stopped due to ShutdownAttribuite. Find all not finished WI
     * handlers and execute them. Executing of not finifhed WI cause continuing in execution of processs.
     * 
     * @return
     */
    public String resumeProcesses();

    /**
     * Resume not finished process. Find all not finished WI handlers and execute them. Executing of not finifhed WI cause
     * continuing in execution of processs.
     * 
     * @return
     */
    public String resumeProcess(long instanceId); 
    
    /**
     * Returns list of processInstanceId of processes which exceed timeout variable. Timeout variable is set for
     * processInstance at create or start. If no value is set during create or start, default value is used.
     * 
     * @return
     */
    public String activeInstancesExceedTimeout();
    
    /**
     * End all process instances which exceed timeout variable. Timeout variable is set for
     * processInstance at create or start. If no value is set during create or start, default value is used. To see list
     * of these instances use method activeInstancesExceedTimeout.
     * 
     * @param initiator
     *          name of initiator which requests abort
     * @return list of processInstanceId which was ended
     */
    public String endInstancesExceedTimeout(String initiator);
}
