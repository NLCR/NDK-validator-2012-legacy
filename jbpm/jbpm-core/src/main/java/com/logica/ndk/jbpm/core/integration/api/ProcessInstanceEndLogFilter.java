package com.logica.ndk.jbpm.core.integration.api;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Rudolf Daco
 */
public class ProcessInstanceEndLogFilter implements Serializable {
  private static final long serialVersionUID = -607797357525724248L;

  private Long id;
  private Long processInstanceId;
  private String processId;
  private Integer state;
  private Date startDateFrom;
  private Date startDateTo;
  private Date endDateFrom;
  private Date endDateTo;
  private Integer maxResult;
  private String orderBy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(Long processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state;
  }

  public Date getStartDateFrom() {
    return startDateFrom;
  }

  public void setStartDateFrom(Date startDateFrom) {
    this.startDateFrom = startDateFrom;
  }

  public Date getStartDateTo() {
    return startDateTo;
  }

  public void setStartDateTo(Date startDateTo) {
    this.startDateTo = startDateTo;
  }

  public Date getEndDateFrom() {
    return endDateFrom;
  }

  public void setEndDateFrom(Date endDateFrom) {
    this.endDateFrom = endDateFrom;
  }

  public Date getEndDateTo() {
    return endDateTo;
  }

  public void setEndDateTo(Date endDateTo) {
    this.endDateTo = endDateTo;
  }

  public Integer getMaxResult() {
    return maxResult;
  }

  public void setMaxResult(Integer maxResult) {
    this.maxResult = maxResult;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public String toString() {
    return "id:" + id + " processInstanceId:" + processInstanceId + " processId:" + processId + " state:" + state + " startDateFrom:" + startDateFrom + " startDateTo:" + startDateTo;
  }
}
