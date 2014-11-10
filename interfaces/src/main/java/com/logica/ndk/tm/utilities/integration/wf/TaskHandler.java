package com.logica.ndk.tm.utilities.integration.wf;

import java.io.IOException;
import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.transform.TransformerException;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Codebook;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.exception.WFConnectionUnavailableException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Scan;
import com.logica.ndk.tm.utilities.integration.wf.task.Signature;

/**
 * Coordinates WF with TM. Provides methods for periodical check of tasks waiting in WF to be
 * processed as well as notification mechanism of finished tasks.
 * 
 * @author majdaf
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface TaskHandler {

  /**
   * Get waiting tasks in WF and start TM processes
   * @throws BadRequestException 
   * @throws IOException
   */
  @WebMethod
  public void handleWaitingTasks() throws WFConnectionUnavailableException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Terminate processes exceeding timeout
   * @param Initirator name
   * @return List of ended instances
   */
  @WebMethod
  @WebResult(name = "codebooks")
  public ArrayList<Long> endInstancesExceededTimeout(
      @WebParam(name = "initiator") String initiator
      ) throws SystemException;
  
  
  
  // FIXME WFAPI temporary support 
  /**
   * Get tasks with relation to a document with given barcode
   * @param barCode Bar code of the document
   * @return List of matching tasks
   * @throws WFConnectionUnavailableException
   * @throws BadRequestException 
   */
  @WebMethod
  @WebResult(name = "tasks")
  public ArrayList<PackageTask> getTasksByBarCode(
      @WebParam(name = "barCode") String barCode) 
      throws WFConnectionUnavailableException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Reserve task in WF
   * @param taskId Task identifier in WF
   * @param userName Reserving user
   * @param agentId Instance handling the task
   * @param note Note displayed in WF
   * @throws WFConnectionUnavailableException
   * @throws BadRequestException
   */
  @WebMethod
  public void reserveTask(
      @WebParam(name = "taskId") Long taskId,
      @WebParam(name = "userName") String userName,
      @WebParam(name = "agentId") String agentId,
      @WebParam(name = "note") String note
  ) 
      throws WFConnectionUnavailableException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Finish task inf WF
   * @param task Finished task containing all result parameters
   * @param signal Signal type for WF
   * @throws WFConnectionUnavailableException
   * @throws BadRequestException
   */
  @WebMethod
  public void finishTask(
      @WebParam(name = "task") FinishedPackageTask task, 
      @WebParam(name = "signal") String signal) 
      throws WFConnectionUnavailableException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Check accessibility of WF
   * @return True if accessible
   * @throws WFConnectionUnavailableException
   */
  @WebMethod
  @WebResult(name = "result")
  public boolean ping() throws BusinessException, SystemException;
  
  /**
   * Retrieve list of scanners defined in WF
   * @return list of scanners defined in WF
   * @throws BadRequestException 
   * @throws IOException 
   */
  @WebMethod
  @WebResult(name = "scanners")
  public ArrayList<Scanner> getScanners() throws IOException, BadRequestException, BusinessException, SystemException;

  /**
   * Retrieve list of scans performed on a given task
   * @param taskId Task ID of task of which scans are to be returned
   * @return List of scans
   * @throws BadRequestException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonParseException 
   */
  @WebMethod
  @WebResult(name = "scans")
  public ArrayList<Scan> getScans(@WebParam(name = "taskId") Long taskId) throws IOException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Retrieve list of signatures associated with a given task
   * @param taskId Task ID of task of which signatures are to be returned
   * @return List of signatures
   * @throws BadRequestException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonParseException 
   */
  @WebMethod
  @WebResult(name = "signatures")
  public ArrayList<Signature> getSignatures(@WebParam(name = "taskId") Long taskId) throws IOException, BadRequestException, BusinessException, SystemException;

  /**
   * Release reservation of task
   * @param taskId Reserved task ID
   * @param userName Releasing user
   * @param note Note
   * @throws BadRequestException 
   * @throws IOException 
   * @throws TransformerException 
   */
  @WebMethod
  public void releaseReservedTaskWithNote(
      @WebParam(name="taskId") Long taskId, 
      @WebParam(name="userName") String userName,
      @WebParam(name="note") String note
  ) throws TransformerException, IOException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Release reservation of task
   * @param taskId Reserved task ID
   * @param userName Releasing user
   * @param note Note
   * @throws BadRequestException 
   * @throws IOException 
   * @throws TransformerException 
   */
  @WebMethod
  public void releaseReservedTask(
      @WebParam(name="taskId") Long taskId, 
      @WebParam(name="userName") String userName
  ) throws TransformerException, IOException, BadRequestException, BusinessException, SystemException;

  /**
   * Create new scan in WF
   * @param scan Scan to create with parameters set
   * @return Scan ID
   * @throws BadRequestException 
   * @throws IOException 
   * @throws TransformerException 
   */
  @WebMethod
  @WebResult(name = "scanId")
  public Long createScan(@WebParam(name = "scan") Scan scan) throws TransformerException, IOException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Finish scan
   * @param scan Scan
   * @throws BadRequestException 
   * @throws IOException 
   * @throws TransformerException 
   */
  @WebMethod
  public void finishScan(@
      WebParam(name = "scan") Scan scan) throws TransformerException, IOException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Get WF task by ID
   * @param taskId Task ID
   * @return Task
   * @throws BadRequestException 
   * @throws TransformerException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonParseException 
   * @throws BadRequestException 
   * @throws TransformerException 
   * @throws IOException 
   * @throws JsonMappingException 
   * @throws JsonParseException 
   */
  @WebMethod
  @WebResult(name = "task")
  public PackageTask getPackageTask(@WebParam(name = "taskId") Long taskId) throws IOException, TransformerException, BadRequestException, BusinessException, SystemException;
  
  /**
   * Get list of codebooks of given type
   * @param cbType
   * @return List of codebooks
   * @throws IOException
   * @throws BadRequestException
   */
  @WebMethod
  @WebResult(name = "codebooks")
  public ArrayList<Codebook> getCodebooks(
      @WebParam(name = "cbType") String cbType
      ) throws IOException, BadRequestException, BusinessException, SystemException;


}
