package com.logica.ndk.tm.master.transformer;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.MessageFactory;
import org.mule.transformer.AbstractMessageTransformer;

/**
 * Pouziva sa na trasformovanie response. Ak je response sprava bez correlationId povazujeme ju za spravu vratenu kovly
 * timeout a preto hodime vynimku.
 * 
 * @author Rudolf Daco
 */
public class EmptyMessageTimeoutTransformer extends AbstractMessageTransformer {

  @Override
  public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
    String correlationId = message.getCorrelationId();
    if (correlationId == null) {
      // no correlationId == "empty" messsage -> toto moze nastat iba ak ubehol timeout pre vykonanie utility a teda nestihol sa do message zapisat correlaitonId
      // toto je sposob ako odlisit timeout. Timeout na urovni CXF WS Server nie je mozne nastavit a element inbound-endpoint nema funkciu na hodenie vynimky v pripade timoeut.
      // Mame tam tiemout ale ten iba spravi to ze sa odpovie s prazdnou spravou a toto vyuzijeme tu aby sme z toho vygenerovali vynimku.
      throw new TransformerException(MessageFactory.createStaticMessage("ResponseTimeout"));
    }
    return message;
  }

}
