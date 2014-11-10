package com.logica.ndk.tm.jbpm.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.jbpm.core.integration.api.ActiveWorkItem;
import com.logica.ndk.tm.jbpm.asyncTimer.FinishRegistedWorkItemsJob;
import com.logica.ndk.tm.jbpm.errortrashold.TrasholdManager;
import com.logica.ndk.tm.jbpm.handler.AbstractAsyncHandler;
import com.logica.ndk.tm.jbpm.handler.ActiveWorkItemManager;
import com.logica.ndk.tm.jbpm.handler.ActiveWorkItemManagerImpl;
import com.logica.ndk.tm.jbpm.handler.ActiveWorkItemManagerLock;
import com.logica.ndk.tm.jbpm.handler.ActiveWorkItemManagerTest;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ExceptionMessage;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author Rudolf Daco
 *
 */
public class ResponseMessageListener implements MessageListener {
  protected final Logger log = LoggerFactory.getLogger(ResponseMessageListener.class);
  public static final String JMS_MSG_MULE_CORRELATION_ID_PROPERTY = "MULE_CORRELATION_ID";
  private static boolean testRun = false;

  private ActiveWorkItemManager activeWorkItemService;

  @SuppressWarnings("unchecked")
  @Override
  public void onMessage(final Message message) {
    log.info("ResponseMessageListener.onMessage: " + message);
    if (testRun == true) {
      activeWorkItemService = new ActiveWorkItemManagerTest();
    }
    else {
      activeWorkItemService = new ActiveWorkItemManagerImpl();
    }
    
    FinishRegistedWorkItemsJob.getInstance();
    
    String correlationId = null;
    ActiveWorkItem activeWorkItem = null;
    try {
      correlationId = getMessageCorrelationId(message);      
      log.info("Message with correlationId {} received, messageId {}", correlationId, message.getJMSMessageID());
      if (correlationId != null) {
        log.info("Get work item");
        activeWorkItem = activeWorkItemService.getWorkItem(correlationId);
        if (activeWorkItem == null) {
          log.info("Going to wait for lock.");
          ActiveWorkItemManagerLock.getInstance().waitForLock();
          activeWorkItem = activeWorkItemService.getWorkItem(correlationId);
          if (activeWorkItem == null) {
            throw new SystemException("ActiveWorkItem not found for correlation id: " + correlationId,ErrorCodes.WORK_ITEM_NOT_FOUND);
          }
        }
        
        log.info("processResponseMessage");
        Object response = processResponseMessage(message);
        if (response instanceof ExceptionMessage) {
          ExceptionMessage exMessage = (ExceptionMessage) response;
          log.error("Processing exception for handler {}. Exception: {}", activeWorkItem.getWorkItemHandlerClass(), exMessage);
          String processId = activeWorkItemService.abortProcessInstanceByWorkItemHandler(activeWorkItem.getProcessInstanceId(), activeWorkItem.getWorkItemHandlerClass(), exMessage);
          log.error("Processing exception for handler {} finished OK", activeWorkItem.getWorkItemHandlerClass());
          TrasholdManager.runControl(processId);
        }
        else {
          AbstractAsyncHandler asyncHandler = ((Class<AbstractAsyncHandler>) Class.forName(activeWorkItem.getWorkItemHandlerClass())).newInstance();
          Map<String, Object> results = asyncHandler.processResponse(response);
          log.info("Results: {}", results);
          activeWorkItemService.completeWorkItem(activeWorkItem, results);
        }
        log.info("delete work item");
        activeWorkItemService.deleteWorkItem(correlationId);
      }
      else {
        log.debug("Received message without correlation id. This messages is discarted: " + message);
      }   
      
    }
    catch (Exception e) {
      if (activeWorkItem != null) {
        log.error("Processing exception for handler {}. Exception: {}", activeWorkItem.getWorkItemHandlerClass(), e);
        activeWorkItemService.abortProcessInstanceByWorkItemHandler(activeWorkItem.getProcessInstanceId(), activeWorkItem.getWorkItemHandlerClass(), e);
        log.error("Processing exception for handler {} finished OK", activeWorkItem.getWorkItemHandlerClass());
        }
      else {
        log.error("Processing exception. Exception: ", e);
        log.error("Processing exception finished OK");
        throw new SystemException("Error while processing message", e);
      }
    }
    
    log.info("Message process.");
  }

  private String getMessageCorrelationId(final Message message) throws JMSException {
    if (message == null) {
      return null;
    }
    return message.getStringProperty(JMS_MSG_MULE_CORRELATION_ID_PROPERTY);
  }

  private Object processResponseMessage(final Message message) throws Exception {
    log.debug("Message from service {} is: {}", getClass().getSimpleName(), message);
    Object result;
    if (message instanceof ObjectMessage) {
      result = ((ObjectMessage) message).getObject();
    }
    else if (message instanceof TextMessage) {
      result = ((TextMessage) message).getText();
    }
    else if (message instanceof StreamMessage) {
      List<Object> list = new ArrayList<Object>();
      StreamMessage streamMessage = (StreamMessage) message;
      boolean end = false;
      while (end == false) {
        try {
          list.add(streamMessage.readObject());
        }
        catch (MessageEOFException e) {
          end = true;
        }
      }
      result = list;
    }
    else {
      throw new SystemException("Unknown message type: " + message.getClass(), ErrorCodes.UNKNOWN_MESSAGE);
    }
    return result;
  }

  public static boolean isTestRun() {
    return testRun;
  }

  public static void setTestRun(boolean testRun) {
    ResponseMessageListener.testRun = testRun;
  }
}
