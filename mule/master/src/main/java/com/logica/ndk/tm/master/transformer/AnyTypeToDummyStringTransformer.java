package com.logica.ndk.tm.master.transformer;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

/*
 * Pouziva sa na pretransformovanie cohokolvek na string. 
 * Ide o to, aby nasledujuci komponent mohol vygenerovat exception.
 */
public class AnyTypeToDummyStringTransformer extends AbstractTransformer {

  @Override
  protected Object doTransform(Object src, String enc) throws TransformerException {
    return src.toString();
  }
}
