package com.logica.ndk.jbpm.core.integration.api;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author rudi
 *
 */
@Entity
public class VariableEndLog implements Serializable {

  private static final long serialVersionUID = -3785703577330654302L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private String name;
  private String value;
  @ManyToOne
  @JoinColumn(name = "processInstanceId", nullable = false)
  private ProcessInstanceEndLog processInstanceEndLog;
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created")
  private Date created;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    if (value != null && value.length() > 255) {
      value = value.substring(0, 255);
    }
    this.value = value;
  }

  public ProcessInstanceEndLog getProcessInstanceEndLog() {
    return processInstanceEndLog;
  }

  public void setProcessInstanceEndLog(ProcessInstanceEndLog processInstanceEndLog) {
    this.processInstanceEndLog = processInstanceEndLog;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public String toString() {
    return "VariableEndLog:  id'" + id + "' name:'" + name + "' value:'" + value + "'";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) id;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((created == null) ? 0 : created.hashCode());
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
    VariableEndLog other = (VariableEndLog) obj;
    if (created == null) {
      if (other.created != null)
        return false;
    }
    else if (!created.equals(other.created))
      return false;
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    }
    else if (!value.equals(other.value))
      return false;
    return true;
  }
}
