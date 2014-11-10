package com.logica.ndk.tm.master.transformer;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomSoapFaultOutInterceptor extends AbstractSoapInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(CustomSoapFaultOutInterceptor.class);

  public CustomSoapFaultOutInterceptor() {
    super(Phase.MARSHAL);
    getAfter().add(Soap11FaultOutInterceptor.class.getName());
  }

  @Override
  public void handleMessage(SoapMessage message) throws Fault {
    Fault fault = (Fault) message.getContent(Exception.class);
    Throwable t = getOriginalCause(fault.getCause());
    LOG.debug("Handling SOAP fault: " + t);
    fault.setMessage(t.getMessage());
  }

  private Throwable getOriginalCause(Throwable t) {
    LOG.debug("Handling SOAP fault, originalCause: " + t.getClass());
    if (t.getCause() == null || t.getCause().equals(t))
      return t;
    else
      return getOriginalCause(t.getCause());
  }
}
