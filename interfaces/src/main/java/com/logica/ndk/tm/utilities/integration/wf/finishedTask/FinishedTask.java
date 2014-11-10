package com.logica.ndk.tm.utilities.integration.wf.finishedTask;

import java.util.List;

/**
 * Represents a finished task passed from TM to WF
 * 
 * @author majdaf
 */
public class FinishedTask { 
  Long id;
  Boolean error;
  String user;
  List<String> errorMessages;
  String note;
  String device;

  public FinishedTask() {
  }
  
  public FinishedTask(Long id) {
    this.id = id;
  }
  
  public FinishedTask(Long id, String user) {
    this.id = id;
    this.user = user;
  }
  
  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Boolean isError() {
    return error;
  }

  public Boolean getError() {
    return error;
  }

  public void setError(Boolean error) {
    this.error = error;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device;
  }


}
