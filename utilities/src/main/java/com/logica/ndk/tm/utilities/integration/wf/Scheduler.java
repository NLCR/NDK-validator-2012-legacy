package com.logica.ndk.tm.utilities.integration.wf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.integration.wf.task.Signature;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.jbpm.FreeProcess;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class Scheduler {
  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  static WFClient wfClient = new WFClient();

  public List<Task> schedule(List<Task> tasks, Map<String, FreeProcess> freeInstances) throws Exception {
    List<Task> plan = new ArrayList<Task>();
    
    for (Task task: tasks) {
      // Get process
      String activity = task.getActivity().getCode();
      String typeDef = TaskHandlerConfig.getActivityTypeDefinition(activity);
      String timesDefString = TaskHandlerConfig.getActivityTimesDefinition(activity);
      Integer timesDef = null;
      
      if(timesDefString != null) {
        timesDef = new Integer(timesDefString);
      }     
      
      String code = null;

      if (typeDef != null) {
        code = (String) getPropertyRecursively(task, typeDef);
        log.debug("Code: " + code);
      }
      
      String processId  = null;
      // check if there are different process ids (w/o code)
      String processId1 = TaskHandlerConfig.getProcessIdByActivity(activity, null);
      String processId2 = TaskHandlerConfig.getProcessIdByActivity(activity, code);
      
      if (!StringUtils.isEmpty(processId1) && !StringUtils.isEmpty(processId2)) {
        // only if there are two different processes ids
        log.debug("Two process IDs {" + processId1 + ", " + processId2 + "} for activity " + activity + " and code {null, " + code + "}");
        if (processId1.equals(processId2)) processId = processId1; // if they are equal, just set process id
        else {
          log.debug("Process IDs are different");
          // if process ids are different
          if (timesDef != null) {
            log.debug("Times definition is set to: " + timesDef.intValue());
            // if times definition is set on process, then get signatures and check how many times such activity was successfully completed before
            List<Signature> signatures =  getSignaturesForTask(task);
            int times = 0;
            log.debug("Reviewing signatures for the package ... ");
            for (Signature signature : signatures) {
              if(signature.getActivityCode().equals(activity) && signature.getSignatureType().contains("Finish") && !signature.isError()) times++;
              if(times >= timesDef) break; // if number of such signatures is reached break
            }
            processId = (times >= timesDef) ? processId1 : processId2;  // set different process ids according to times definition
            log.debug("Finished reviewing signatures, chosen process ID: " + processId + " from {" + processId1 + ", " + processId2 + "}");
          } else {
            processId = TaskHandlerConfig.getProcessIdByActivity(activity, code);
          }
          
        }
      } else {
        // only if there is one process ids
        processId = TaskHandlerConfig.getProcessIdByActivity(activity, code);
        log.debug("Only one process ID " + processId + " for activity " + activity + " and code " + code);
      }
      
      log.debug("Final process ID: " + processId);
      
      if (processId == null) {
        log.error("Unknown process ID for activity " + activity + " and code " + code);
        continue;
      }
      if (freeInstances.get(processId) == null) {
        log.error("Free instance count for process ID " + processId + " not defined");
        continue;
      }
      if (freeInstances.get(processId).getCount() > 0) {
        plan.add(task);
        freeInstances.get(processId).setCount(freeInstances.get(processId).getCount()-1);
      } else {
        log.debug("No free instances for process " + processId);
      }
      
    }
    
    return plan;
  }

  // TODO !!! Duplicate code with TaskHandlerImpl
  public Object getPropertyRecursively(Object o, String properties) throws Exception {

    int index = properties.indexOf(".");
    if (index >= 0) {
      String propertyBase = properties.substring(0, index);
      String remainingProperties = properties.substring(index + 1);
      String method = getMethodName(propertyBase);
      Object property = invoke(o, method, new Class[] {}, new Object[] {});
      if (property == null) {
        return null;
      }
      return getPropertyRecursively(property, remainingProperties);
    }
    else {
      String method = getMethodName(properties);
      return invoke(o, method, new Class[] {}, new Object[] {});
    }
  }

  private static String getMethodName(String base) {
    return "get" + base.substring(0, 1).toUpperCase() + base.substring(1);
  }

  @SuppressWarnings("unchecked")
  private static Object invoke(Object aObject, String aMethod, Class[] params, Object[] args) throws Exception {
    Class c = aObject.getClass();
    Method m = c.getMethod(aMethod, params);
    Object r = m.invoke(aObject, args);
    return r;
  }
  
  public List<Signature> getSignaturesForTask(Task task) throws Exception {
    return wfClient.getSignatures(task.getId());
  }

}
