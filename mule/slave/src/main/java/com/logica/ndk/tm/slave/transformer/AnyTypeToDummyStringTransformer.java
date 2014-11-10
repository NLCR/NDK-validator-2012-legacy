package com.logica.ndk.tm.slave.transformer;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

/*
 * Pouziva sa na pretransformovanie cohokolvek na string. 
 * Ide o to, aby nasledujuci komponent mohol vygenerovat exception v pripade ak pre prichadzaujucu spravu nemame zodpovedajuci handler na strane slave.
 */
public class AnyTypeToDummyStringTransformer extends AbstractTransformer {

  @Override
  protected Object doTransform(Object src, String enc) throws TransformerException {
    return src.toString();
  }
}
