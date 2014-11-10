package com.logica.ndk.tm.slave.transformer;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.component.ComponentException;
import org.mule.message.ExceptionMessage;
import org.mule.transformer.AbstractMessageTransformer;

import com.logica.ndk.tm.utilities.ErrorHelper;

public class ExceptionMessageTransformer extends AbstractMessageTransformer {

  @Override
  public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
    final Object payload = message.getPayload();
    if (payload instanceof ExceptionMessage) {
      ExceptionMessage muleExMessage = (ExceptionMessage) payload;
      final Throwable exception = ((ExceptionMessage) payload).getException();
      if (exception instanceof ComponentException) {
        Throwable cause = ((ComponentException) exception).getCause();
        com.logica.ndk.tm.utilities.ExceptionMessage exMessage = new com.logica.ndk.tm.utilities.ExceptionMessage(cause.getMessage());
        exMessage.setExceptionName(cause.getClass().getName());
        exMessage.setStackTrace(cause.getStackTrace());
        exMessage.setErrorCode(ErrorHelper.getErrorCode(cause));
        exMessage.setTimeStamp(muleExMessage.getTimeStamp());
        exMessage.setComponentName(muleExMessage.getComponentName());
        exMessage.setEndpointUri(muleExMessage.getEndpoint());
        Object nodeId = message.getInboundProperty("NODE_ID");
        if (nodeId != null) {
          exMessage.setNodeId((String) nodeId);
        }
        message.setPayload(exMessage);
      }
    }
    return message;
  }
}
