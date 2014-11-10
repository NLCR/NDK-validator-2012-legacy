package com.logica.ndk.jbpm.core.integration.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.drools.runtime.Environment;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLog;
import com.logica.ndk.jbpm.core.integration.api.ProcessInstanceEndLogFilter;

/**
 * Use this class to implement methods to FIND or GET some LOG data. Do not use to delete or update (use class
 * JPAWorkingMemoryDbLoggerExtend for this). Do not use to find
 * or get other than LOG data.
 * 
 * @author Rudolf Daco
 */
public class JPAProcessInstanceDbLogExtend extends JPAProcessInstanceDbLog {
  private static final Logger LOG = LoggerFactory.getLogger(JPAProcessInstanceDbLogExtend.class);

  public JPAProcessInstanceDbLogExtend(Environment environment) {
    super(environment);
  }  

  @SuppressWarnings("unchecked")
  public List<ProcessInstanceLog> findActiveProcessInstances() {//throws ServiceException {
	  EntityManager em = null;
	  List<ProcessInstanceLog> result = null;
      try {
        em = getEntityManager();
        result = em.createQuery("FROM ProcessInstanceLog p WHERE p.end is null order by processInstanceId DESC").getResultList();      
      }
      finally {
       if (em != null) {    	   
    	   em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
    	   em.close(); // and this closes the entity manager         
    	   em = null;
       }
     }    
    return result;
  }

  public ProcessInstanceEndLog findProcessInstanceEndLog(long processInstanceId) {
    ProcessInstanceEndLog result = null;
    EntityManager em = null;
    try {
        em = getEntityManager();
	    try {
	      result = (ProcessInstanceEndLog) em
	          .createQuery("FROM ProcessInstanceEndLog p WHERE p.processInstanceId = :processInstanceId order by processInstanceId DESC")
	          .setParameter("processInstanceId", processInstanceId).getSingleResult();
	    }
	    catch (NoResultException e) {
	      // no result was found
	    }
    } finally {
    	if (em != null) {    		
     	    em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
     	    em.close(); // and this closes the entity manager     	
     	    em = null;
      }
   }    
	    
    return result;
  }

  @SuppressWarnings("unchecked")
  public List<ProcessInstanceEndLog> findProcessInstanceEndLog(ProcessInstanceEndLogFilter filter) {
    LOG.info("filter: " + filter);
    StringBuffer where = new StringBuffer(" where 1 = 1 ");
    Map<String, Object> parameters = new HashMap<String, Object>();
    if (filter.getStartDateFrom() != null) {
      where.append(" and startDate >= :startDateFrom ");
      parameters.put("startDateFrom", filter.getStartDateFrom());
    }
    if (filter.getStartDateTo() != null) {
      where.append(" and startDate <= :startDateTo ");
      parameters.put("startDateTo", filter.getStartDateTo());
    }
    if (filter.getEndDateFrom() != null) {
        where.append(" and endDate >= :endDateFrom ");
        parameters.put("endDateFrom", filter.getEndDateFrom());
	}
	if (filter.getEndDateTo() != null) {
	  where.append(" and endDate <= :endDateTo ");
	  parameters.put("endDateTo", filter.getEndDateTo());
	}
	  
    if (filter.getId() != null) {
      where.append(" and id = :id ");
      parameters.put("id", filter.getId());
    }
    if (filter.getProcessInstanceId() != null) {
      where.append(" and processInstanceId = :processInstanceId ");
      parameters.put("processInstanceId", filter.getProcessInstanceId());
    }
    if (filter.getProcessId() != null) {
      where.append(" and processId = :processId ");
      parameters.put("processId", filter.getProcessId());
    }
    if (filter.getState() != null) {
      where.append(" and state = :state ");
      parameters.put("state", filter.getState());
    }
    String orderBy = "processInstanceId";
    if(filter.getOrderBy() != null){
      orderBy = filter.getOrderBy();
    }
    String queryString = "FROM ProcessInstanceEndLog p " + where + " order by " + orderBy + " DESC ";
    LOG.debug(queryString);
    EntityManager em = null;
    Query query = null;
    List<ProcessInstanceEndLog> result = null;
    try {
        em = getEntityManager();
        query = em.createQuery(queryString);
	    for (String key : parameters.keySet()) {
	      query.setParameter(key, parameters.get(key));
	    }
	    
	    if(filter.getMaxResult() != null){
	      query.setMaxResults(filter.getMaxResult());
	    }
	    
	    result = query.getResultList();
    }  finally {
    	if (em != null) {    	   
     	   em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
     	   em.close(); // and this closes the entity manager 
    	   em = null;
      }
   }       
    return result;
  }
  
  @Override
  @SuppressWarnings("unchecked")
	public List<NodeInstanceLog> findNodeInstances(long processInstanceId) {
	  EntityManager em = null;
  	  List<NodeInstanceLog> result = null;
  	  try {
  		  em = getEntityManager();
		  result = em.createQuery("FROM NodeInstanceLog n WHERE n.processInstanceId = :processInstanceId ORDER BY date")
				.setParameter("processInstanceId", processInstanceId).getResultList();
  	  } finally {
      	if (em != null) {      		
     	    em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
     	    em.close(); // and this closes the entity manager 
        	em = null;
          }
       }     	  
	return result;
  }
  
  @Override  
  public ProcessInstanceLog findProcessInstance(long processInstanceId) {
	  EntityManager em = null;
  	  ProcessInstanceLog result = null;
  	  try {
  		  em = getEntityManager();
  		  result = (ProcessInstanceLog)em.createQuery("FROM ProcessInstanceLog p WHERE p.processInstanceId = :processInstanceId")
			.setParameter("processInstanceId", processInstanceId).getSingleResult();
  	  } finally {
    	if (em != null) {    		
     	    em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
     	    em.close(); // and this closes the entity manager 
        	em = null;
         }
      }   			
	 return result;
  }
  
  @SuppressWarnings("unchecked")
  @Override
	public List<ProcessInstanceLog> findActiveProcessInstances(String processId) {
	  EntityManager em = null;
	  List<ProcessInstanceLog> result = null;
  	  try {
  		  em = getEntityManager();
  		  result = em.createQuery("FROM ProcessInstanceLog p WHERE p.processId = :processId AND p.end is null")
			.setParameter("processId", processId).getResultList();  		  
  	  } finally {
    	if (em != null) {    		
     	    em.clear(); // This makes sure that any returned entities are no longer attached to this entity manager/persistence context
     	    em.close(); // and this closes the entity manager 
        	em = null;
          }
      }   
	 return result;
	}
  
}
