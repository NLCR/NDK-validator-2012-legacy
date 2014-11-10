package com.logica.ndk.jbpm.core.integration.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author rudi
 *
 */
@Entity
public class ProcessInstanceEndLog implements Serializable {

  private static final long serialVersionUID = 510l;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private long processInstanceId;
  private String processId;
  private int state;
  @OneToMany(mappedBy = "processInstanceEndLog", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private Collection<VariableEndLog> variableList;
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate;
  @Temporal(TemporalType.TIMESTAMP)
  private Date endDate;  

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(long processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Collection<VariableEndLog> getVariableList() {
    return variableList;
  }

  public void setVariableList(Collection<VariableEndLog> variableList) {
    this.variableList = variableList;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String toString() {
    return "ProcessInstanceEndLog: processId'" + processId + "' processInstanceId:'" + processInstanceId + "' state:'" + state + "'";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((created == null) ? 0 : created.hashCode());
    result = prime * result + (int) id;
    result = prime * result + state;
    result = prime * result
        + ((processId == null) ? 0 : processId.hashCode());
    result = prime * result + (int) processInstanceId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProcessInstanceEndLog other = (ProcessInstanceEndLog) obj;
    if (created == null) {
      if (other.created != null)
        return false;
    }
    else if (!created.equals(other.created))
      return false;
    if (id != other.id)
      return false;
    if (state != other.state)
      return false;
    if (processId == null) {
      if (other.processId != null)
        return false;
    }
    else if (!processId.equals(other.processId))
      return false;
    if (processInstanceId != other.processInstanceId)
      return false;
    return true;
  }
}
