package com.logica.ndk.tm.master.transformer;

import java.io.Serializable;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

import com.logica.ndk.tm.utilities.sample.ParamWrapper;

public class ObjectArrayToParamWrapperTransformer extends AbstractTransformer {

  @Override
  public Object doTransform(Object src, String enc) throws TransformerException {

    if (src instanceof Object[]) {
      ParamWrapper wrapper = new ParamWrapper();
      wrapper.setObjArray((Object[]) src);
      return wrapper;
    }

    if (src instanceof Serializable) {
        return src;
    }

    throw new TransformerException(this, new RuntimeException("source is of incorrect type: " + src.getClass().getName()));
  }
}
