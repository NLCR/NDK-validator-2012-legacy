package com.logica.ndk.tm.jbpm.ws;

import java.util.List;

import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Header handler for WS calls for sync utils.
 * 
 * @author Rudolf Daco
 */
public class HeaderHandlerSyncService extends AbstractHeaderHandler {
  protected final Logger log = LoggerFactory.getLogger(HeaderHandlerSyncService.class);
  
  public HeaderHandlerSyncService() {
    super();
  }

  public HeaderHandlerSyncService(List<ParamUtility> paramUtility) {
    super(paramUtility);
  }

  protected boolean handle(SOAPMessageContext smc) {
    Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    try {
      if (outboundProperty == true) {
        handlePriorityForOutboundMessage(smc);
      }
    }
    catch (SOAPException e) {
      log.error("Error at parsing SOAP header from WS response.", e);
      return false;
    }
    return true;
  }
}
