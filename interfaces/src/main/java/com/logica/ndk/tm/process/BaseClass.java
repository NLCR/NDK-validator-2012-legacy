package com.logica.ndk.tm.process;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Hlavna trieda od ktorej dedia niektore generovane triedy ktore sa generuju z XSD. Tymto sme zabezpecili ze generovane
 * triedy maju toString metodu. Pouziva sa to napr. v ActiveMQ admin konzole. toString sa robi pomocou reflections.
 * 
 * @author Rudolf Daco
 */
public class BaseClass implements Serializable {
  private static final long serialVersionUID = -8440118815275304813L;

  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
