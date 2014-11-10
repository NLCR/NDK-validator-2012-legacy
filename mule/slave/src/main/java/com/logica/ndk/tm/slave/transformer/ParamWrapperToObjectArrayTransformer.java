package com.logica.ndk.tm.slave.transformer;

import java.io.Serializable;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.logica.ndk.tm.utilities.sample.ParamWrapper;

public class ParamWrapperToObjectArrayTransformer extends AbstractMessageTransformer {

  @Override
  public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
    final Object src = message.getPayload();

    if (src instanceof ParamWrapper) {
      return ((ParamWrapper) src).getObjArray();
    }

    if (src instanceof Serializable) {
      return src;
    }

    throw new TransformerException(this, new RuntimeException("source is of incorrect type: " + src.getClass().getName()));
  }
}
