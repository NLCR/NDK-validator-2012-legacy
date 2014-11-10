package com.logica.ndk.tm.master.transformer;

import org.apache.cxf.interceptor.Fault;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.component.ComponentException;
import org.mule.message.DefaultExceptionPayload;
import org.mule.message.ExceptionMessage;
import org.mule.transformer.AbstractMessageTransformer;

import com.logica.ndk.tm.utilities.UtilityException;

public class ExceptionMessageTransformer extends AbstractMessageTransformer {

  @Override
  public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
    final Object payload = message.getPayload();
    if (payload instanceof ExceptionMessage) {
      final Throwable exception = ((ExceptionMessage) payload).getException();
      if (exception instanceof ComponentException) {
        Throwable cause = ((ComponentException) exception).getCause();
        if (cause instanceof UtilityException) {
          UtilityException utilEx = (UtilityException) cause;
          Object nodeId = message.getInboundProperty("NODE_ID");
          if (nodeId != null) {
            utilEx.setNodeId((String) nodeId);
          }
        }
        message.setExceptionPayload(new DefaultExceptionPayload(new Fault(cause)));
        message.setPayload(null);
      }
    }
    return message;
  }
}
