/**
 * 
 */
package com.logica.ndk.jbpm.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author brizat
 *
 */
@XmlRootElement
public class ProcessRuntimeConfig {

  private List<ProcessConfig> process;

  public List<ProcessConfig> getProcess() {
    return process;
  }

  public void setProcess(List<ProcessConfig> process) {
    this.process = process;
  }
  
  
}
