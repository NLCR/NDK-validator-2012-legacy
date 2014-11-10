package com.logica.ndk.tm.jbpm.ws;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.process.util.ParamUtility;

/**
 * Handler for SOAP WS request/response.
 * 
 * @author Rudolf Daco
 */
public abstract class AbstractHeaderHandler implements SOAPHandler<SOAPMessageContext> {
  protected final Logger log = LoggerFactory.getLogger(AbstractHeaderHandler.class);
  private List<ParamUtility> paramUtility;

  public AbstractHeaderHandler() {
  }

  public AbstractHeaderHandler(List<ParamUtility> paramUtility) {
    this.paramUtility = paramUtility;
  }

  public Set<QName> getHeaders() {
    return null;
  }

  public boolean handleMessage(SOAPMessageContext smc) {
    return handle(smc);
  }

  public boolean handleFault(SOAPMessageContext smc) {
    return handle(smc);
  }

  // nothing to clean up
  public void close(MessageContext messageContext) {
  }

  protected abstract boolean handle(SOAPMessageContext smc);

  /* 
   * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   * <soap:Header>
   * <priority xmlns="http://wwww.logica.com/ndk/tm/process">3</priority>
   * </soap:Header>
   * <soap:Body>
   * <ns2:executeAsync xmlns:ns2="http://wwww.logica.com/ndk/tm/process">
   * <error>false</error>
   * </ns2:executeAsync>
   * </soap:Body>
   * </soap:Envelope>
  */
  /**
   * Nastavi prioritu do hlavicku WS spravy ktora sa odosiela do Mule. Mule ju vycita a pouzije pre nastavenie priority
   * JMS spravy ktora sa vklada na distributionQueue (pozri CustomSoapInInterceptor). Sluzi to na nastavenie priority
   * vykonania utility.
   * 
   * @param smc
   * @throws SOAPException
   */
  protected void handlePriorityForOutboundMessage(SOAPMessageContext smc) throws SOAPException {
    if (paramUtility != null && paramUtility.size() > 0) {
      SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
      SOAPHeader header = envelope.addHeader();
      for (ParamUtility param : paramUtility) {
        if (param.getValue() != null) {
          log.debug("parameter to set into SOAPHeader: name: " + param.getName() + " value: " + param.getValue());
          SOAPElement el = header.addHeaderElement(new QName(param.getNamespace(), param.getName()));
          el.setValue(param.getValue().toString());
        }
      }
    }
  }

  public List<ParamUtility> getParamUtility() {
    return paramUtility;
  }

  public void setParamUtility(List<ParamUtility> paramUtility) {
    this.paramUtility = paramUtility;
  }

}
