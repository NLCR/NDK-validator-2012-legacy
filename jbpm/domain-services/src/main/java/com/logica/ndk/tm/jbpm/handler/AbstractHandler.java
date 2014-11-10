package com.logica.ndk.tm.jbpm.handler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.jbpm.ws.AbstractHeaderHandler;
import com.logica.ndk.tm.jbpm.ws.EndPoint;
import com.logica.ndk.tm.jbpm.ws.HeaderHandlerAsyncService;
import com.logica.ndk.tm.jbpm.ws.HeaderHandlerResolver;
import com.logica.ndk.tm.jbpm.ws.HeaderHandlerSyncService;
import com.logica.ndk.tm.process.util.ParamUtility;
import com.logica.ndk.tm.process.util.ParamUtilityPriority;
import com.logica.ndk.tm.process.util.ParamUtilityProcessInstanceId;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Parent for all handlers.
 * 
 * @author ondrusekl
 */
public abstract class AbstractHandler implements WorkItemHandler {

  private SpringContextHolder contextHelper;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private static boolean testRun = false;
  
  protected ActiveWorkItemManager activeWorkItemManager;
  
  /**
   * Specifies the amount of time, in milliseconds, that the client will attempt to establish a connection before it times out
   */
  private final static long CONNECTION_TIMEOUT = TmConfig.instance().getLong("process.muleWsClient.connectionTimeout", new Long(60000));
  /**
   * Specifies the amount of time, in milliseconds, that the client will wait for a response before it times out. 
   */
  private final static long RECEIVE_TIMEOUT = TmConfig.instance().getLong("process.muleWsClient.receiveTimeout", new Long(60000));

   public AbstractHandler() {
    if (contextHelper == null) {
      contextHelper = SpringContextHolder.getInstance();
    }
    if(activeWorkItemManager == null) {
      if(testRun == true) {
        activeWorkItemManager = new ActiveWorkItemManagerTest();
      } else {
        activeWorkItemManager = new ActiveWorkItemManagerImpl();
      }
    }
  }
  
  @SuppressWarnings("unused")
  // only for tests
  private AbstractHandler(SpringContextHolder contextHelper) {
    this.contextHelper = contextHelper;
  }
  
  public abstract void executeWorkItem(WorkItem workItem, WorkItemManager manager);

  protected Map<String, Object> executeWorkItemDryRun(WorkItem workItem) {
    log.debug("Dry run does nothing");
    return null;
  }

  /* 
   * This is called if process is terminated and workItem is active.
   */
  @Override
  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
    final String hName = this.getClass().getSimpleName();
    log.info("Default abort handler {}", hName);
  }

  protected <T> T getClient(Class<T> clazz) {
    return contextHelper.getContext().getBean(clazz);
  }

  protected Object getEndPoint(String name) {
    return contextHelper.getContext().getBean(name);
  }

  protected <T> T createDefaultClientService(EndPoint endPoint, Class<T> serviceEndpointInterface, AbstractHeaderHandler headerHandler) {
    Service service = Service.create(endPoint.getServiceNameObject());
    service.addPort(endPoint.getPortNameObject(), SOAPBinding.SOAP11HTTP_BINDING, endPoint.getAddress());
    service.setHandlerResolver(new HeaderHandlerResolver(headerHandler));
    T port = service.getPort(serviceEndpointInterface);
    Client client = ClientProxy.getClient(port);
    HTTPConduit http = (HTTPConduit) client.getConduit();
    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    // Specifies the amount of time, in milliseconds, that the client will attempt to establish a connection before it times out
    httpClientPolicy.setConnectionTimeout(CONNECTION_TIMEOUT);
    // Specifies the amount of time, in milliseconds, that the client will wait for a response before it times out. 
    httpClientPolicy.setReceiveTimeout(RECEIVE_TIMEOUT);
    http.setClient(httpClientPolicy);    
    return port;
  }
  
  @SuppressWarnings("unchecked")
  protected <T> T unmarshall(String payload) {
    try {
      return (T) JAXBContextPool.getContext("com.logica.ndk.tm.process").createUnmarshaller().unmarshal(new StringReader(payload));
    }
    catch (JAXBException e) {
      throw new SystemException("Creating unmarshaller failed. Stacktrace: "+e,ErrorCodes.JAXB_UNMARSHALL_ERROR);
    }
  }

  protected class AsyncCallInfo<T> {
    private final HeaderHandlerAsyncService hdr;
    private final T client;

    public AsyncCallInfo(String endpointName, Class<T> clientClass, final List<ParamUtility> paramUtility) {
      this.hdr = new HeaderHandlerAsyncService(paramUtility);
      this.client = createDefaultClientService((EndPoint) getEndPoint(endpointName), clientClass, this.hdr);
    }

    public T getClient() {
      return client;
    }

    public String getCorrelationId() {
      final String cid = this.hdr.getCorrelationIdFromHeader();
      if (cid == null) {
        throw new NullPointerException("No correlation Id returned from client " + client);
      }
      log.debug("Correlation Id is: {}", cid);
      return cid;
    }

  }
  
  protected class SyncCallInfo<T> {
    private final HeaderHandlerSyncService hdr;
    private final T client;

    public SyncCallInfo(String endpointName, Class<T> clientClass, final List<ParamUtility> paramUtility) {
      this.hdr = new HeaderHandlerSyncService(paramUtility);
      this.client = createDefaultClientService((EndPoint) getEndPoint(endpointName), clientClass, this.hdr);
    }

    public T getClient() {
      return client;
    }
  }
  
  protected List<ParamUtility> prepareUtilityParameters(WorkItem workItem) {
    List<ParamUtility> result = new ArrayList<ParamUtility>();
    result.add(new ParamUtilityProcessInstanceId(String.valueOf(workItem.getProcessInstanceId())));
    result.add(getPriorityFromParams(workItem));
    return result;
  }
  
  /**
   * Zistuje hodnotu priority z parametrov WI handler za ucelom nastavit prioritu vykonania utility v Mule. Mozne
   * parametre su
   * UtilityPriority.PARAMETER_NAME_DEFAULT - reprezentuje default hodnotu a
   * UtilityPriority.PARAMETER_NAME - reprezentuje runtime hodnotu moznu menit pre instakciu procesu.
   * Parameter s nazvom UtilityPriority.PARAMETER_NAME_DEFAULT sa nastavuje v jBPM
   * designer v danom service task na konkretnu hodnotu.
   * Hodnota parametra UtilityPriority.PARAMETER_NAME sa nastavuje mapovanim v jBPM deisgner v danom service task - dava
   * sa tam
   * mapovenia processnej variable na parametra WI handlera - tym sa prenesie hodnota process variable az do WI
   * handlera.
   * Vyhodnotenie priorit je v nasledujucom poradi. Ak dany prameter neexistuje alebo je null nasleduje sa v dalsom
   * kroku vyhodnotenia:
   * 1. pouzije sa hodnota parametra s nazvom UtilityPriority.PARAMETER_NAME
   * 2. pouzije sa hodnota parametra s nazvom UtilityPriority.PARAMETER_NAME_DEFAULT
   * 3. pouzije sa default hodnota priority rovna UtilityPriority.DEFAULT_VALUE.
   * Ak je aj tato hodnota null tak sa do Mule neodosle nic a JMS messaging (ActiveMQ) pouzije svoje default hodnoty pre
   * spravu (4).
   * 
   * @param workItem
   * @return hodontu priority ktora sa pouziva na odoslanie v Mule v hlavicke WS spravy (pozri AbstractHeaderHandler). V
   *         mule sa potom pouzije na
   *         nastavenie priority spravy ktora je vlozena do distributionQueue (pozri CustomSoapInInterceptor).
   */
  protected ParamUtilityPriority getPriorityFromParams(WorkItem workItem) {
    Integer priority = null;
    String priorityStr = null;
    if (workItem.getParameter(ParamUtilityPriority.PARAMETER_NAME) != null) {
      priorityStr = workItem.getParameter(ParamUtilityPriority.PARAMETER_NAME).toString();
    }
    else if (workItem.getParameter(ParamUtilityPriority.PARAMETER_NAME_DEFAULT) != null) {
      priorityStr = workItem.getParameter(ParamUtilityPriority.PARAMETER_NAME_DEFAULT).toString();
    }
    if (priorityStr != null && priorityStr.length() > 0) {
      try {
        priority = Integer.valueOf(priorityStr);
      }
      catch (Exception e) {
        log.warn("Incorrect format of priority parameter. Number is expected but value is: " + priority);
      }
    }
    if (priority == null) {
      priority = ParamUtilityPriority.DEFAULT_VALUE;
    }
    if (priority != null) {
      return new ParamUtilityPriority(priority.toString());
    }
    else {
      return new ParamUtilityPriority(null);
    }
  }
  
  protected String resolveParam(String parameter, Map<String, Object> otherParameters) {
    return StrPlaceholder.resolveParam(parameter, otherParameters);
  }

  public static boolean isTestRun() {
    return testRun;
  }

  public static void setTestRun(boolean testRun) {
    AbstractHandler.testRun = testRun;
  }
}
