package com.logica.ndk.tm.master.transformer;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

import com.logica.ndk.tm.process.FindRecordResult;

public class FindRecordResultTransformer extends AbstractTransformer {

  @Override
  protected Object doTransform(Object src, String enc) throws TransformerException {
    // TODO Auto-generated method stub
    return new FindRecordResult();
  }
}
