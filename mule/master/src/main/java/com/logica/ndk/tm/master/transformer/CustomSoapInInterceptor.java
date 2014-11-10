package com.logica.ndk.tm.master.transformer;

import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.module.cxf.CxfConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.logica.ndk.tm.process.util.ParamUtilityPriority;

/**
 * Custom SOAP iterceptor used for incoming WS messages to process some data from SOPAHeader.
 * 
 * @author Rudolf Daco
 */
public class CustomSoapInInterceptor extends AbstractSoapInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(CustomSoapInInterceptor.class);
  protected final static String MULE_JMS_PRIORITY = "priority";

  public CustomSoapInInterceptor() {
    super(Phase.PRE_INVOKE);
  }

  /* 
   * Ziska priority z hlavicky JMS spravy a nastavy ju ako JMS prioritu pre messaging system. 
   * Tymto sa vlastne nastavi priority JMS spravy ktora je vkladana do distributionQueue. 
   * Sluzi to na nastavenie priority vykonania utility.
   * @see org.apache.cxf.interceptor.Interceptor#handleMessage(org.apache.cxf.message.Message)
   */
  @Override
  public void handleMessage(SoapMessage message) throws Fault {
    MuleEvent event = (MuleEvent) message.getExchange().get(CxfConstants.MULE_EVENT);
    MuleMessage muleMsg = event.getMessage();
    List<Header> headers = message.getHeaders();
    if (headers != null) {
      for (Header header : headers) {
        if (header != null && header.getObject() != null && header.getObject() instanceof Node) {
          Node node = (Node) header.getObject();
          if (node.getLocalName() != null && node.getTextContent() != null) {
            LOG.debug("parameter from SOAPHeader: name: " + node.getLocalName() + " value: " + node.getTextContent());
            if (node.getLocalName().equals(ParamUtilityPriority.NAME)) {
              // priority sa nastavuje do JMS spravy pod specialnym menom aby to ActiveMQ povazoval za prioritu spravy
              muleMsg.setOutboundProperty(MULE_JMS_PRIORITY, node.getTextContent());
            }
            else {
              muleMsg.setOutboundProperty(node.getLocalName(), node.getTextContent());
            }
          }
        }
      }
    }
  }
}
