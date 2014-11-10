package com.logica.ndk.tm.jbpm.ws;

import java.util.List;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Handler to get correlation id from SOAP response header during calling WS for Asynch service. It parses response from
 * WS and read correlation id into property of this class.
 * 
 * @author Rudolf Daco
 */
public class HeaderHandlerAsyncService extends AbstractHeaderHandler {
  protected final Logger log = LoggerFactory.getLogger(HeaderHandlerAsyncService.class);
  private final static String MULE_CORRELATION_ID = "mule:MULE_CORRELATION_ID";
  private String correlationIdFromHeader;
  
  public HeaderHandlerAsyncService() {
    super();
  }

  public HeaderHandlerAsyncService(List<ParamUtility> paramUtility) {
    super(paramUtility);
  }

  /*
   * Check the MESSAGE_OUTBOUND_PROPERTY in the context
   * to see if this is an outgoing or incoming message.
   * Write a brief message to the print stream and
   * output the message. The writeTo() method can throw
   * SOAPException or IOException
   */
  protected boolean handle(SOAPMessageContext smc) {
    Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    try {
      if (outboundProperty == true) {
        handlePriorityForOutboundMessage(smc);
      }
      else {
        SOAPHeader soapHeader = smc.getMessage().getSOAPHeader();
        if (soapHeader != null) {
          NodeList elementsByTagName = soapHeader.getElementsByTagName(MULE_CORRELATION_ID);
          if (elementsByTagName != null) {
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
              Node item = elementsByTagName.item(i);
              if (item != null) {
                correlationIdFromHeader = item.getTextContent();
              }
            }
          }
        }
      }
    }
    catch (SOAPException e) {
      log.error("Error at parsing SOAP header from WS response.", e);
      return false;
    }
    return true;
  }
  
  public String getCorrelationIdFromHeader() {
    return correlationIdFromHeader;
  }

  public void setMessageIdFromHeader(String messageIdFromHeader) {
    this.correlationIdFromHeader = messageIdFromHeader;
  }
}
