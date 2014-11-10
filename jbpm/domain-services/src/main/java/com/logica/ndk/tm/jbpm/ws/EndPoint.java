package com.logica.ndk.tm.jbpm.ws;

import javax.xml.namespace.QName;

/**
 * @author Rudolf Daco
 *
 */
public class EndPoint {
  private String namespace;
  private String serviceName;
  private String address;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String endpointAddress) {
    this.address = endpointAddress;
  }

  public QName getServiceNameObject() {
    return new QName(namespace, serviceName + "Service");
  }
  
  public QName getPortNameObject() {
    return new QName(namespace, serviceName + "Port");
  }
}
