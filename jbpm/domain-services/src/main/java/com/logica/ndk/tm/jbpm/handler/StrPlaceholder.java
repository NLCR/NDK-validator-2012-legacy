package com.logica.ndk.tm.jbpm.handler;

import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;

/**
 * Triade pre nahradzovanie tokenov v retazci.
 * 
 * @author Rudolf Daco
 */
public class StrPlaceholder {
  /**
   * Constant for the default escape character.
   */
  public static final char DEFAULT_ESCAPE = '$';
  /**
   * Constant for the default variable prefix.
   */
  public static final String DEFAULT_PREFIX = "${";
  /**
   * Constant for the default variable suffix.
   */
  public static final String DEFAULT_SUFFIX = "}";
  
  public static final String CDM_DATA_DIR = "DATA_DIR";
  
  // temporary CDM
  private static final CDM MYCDM = new CDM();


  /**
   * Nahradi vsetky tokeny v danom parametri a vrati novu hodnotu. Ak sa nevie najst hodnota pre token tak sa token
   * nenahradi.
   * 
   * @param parameter
   * @param parameters
   * @return
   */
  public static String resolveParam(String parameter, Map<String, Object> otherParameters) {
    if (parameter == null) {
      return null;
    }
    StrSubstitutor subst = new StrSubstitutor(new StrPlaceholder().new ParameterStrLookup(otherParameters), DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_ESCAPE);
    return subst.replace(parameter);
  }

  /**
   * Nahradzuje tokeny hodnotami v tomto poradi, ak sa hodnota nenajde pokracuje sa v
   * kroku ktory je dalsi v poradi. Ak sa hodnota nenajde token sa nenahradi a zostane v retazci nezmeneny.
   * 1. z hashmapy parameters - Tato hash-mapa je najcastejsie mapa vsetkych parametrov handlera. (napr. cdmId)
   * 2. z CDM - resolvne sa na zaklade enum-u CDMSchemaDir pricom musi existovat v zozname parametrov parameter cdmId. (napr. MC_DIR)
   * 3. z TmConfig - hlada s aproperty s tymto nazvom cez TmConfig (napr. cygwinHome)
   * 
   * @author Rudolf Daco
   */
  public class ParameterStrLookup extends StrLookup {
    private Map<String, Object> parameters;

    public ParameterStrLookup(Map<String, Object> parameters) {
      this.parameters = parameters;
    }

    @Override
    public String lookup(String key) {
      String value = null;
      if (parameters != null) {
        Object object = parameters.get(key);
        if (object != null && object instanceof String) {
          value = (String) object;
        }
      }
      if (value == null && parameters.get("cdmId") != null) {
        value = getCDMPath((String) parameters.get("cdmId"), key);
      }
      if (value == null) {
        value = TmConfig.instance().getString(key, null);
      }
      return value;
    }

    private String getCDMPath(String cdmId, String dirLabel) {
      try {
        if (CDM_DATA_DIR.equals(dirLabel)) {
          return MYCDM.getCdmDataDir(cdmId).getAbsolutePath();
        }
        return MYCDM.getDir(cdmId, dirLabel).getAbsolutePath();
      }
      catch (IllegalArgumentException e) {
        return null;
      }
    }

    public Map<String, Object> getParameters() {
      return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
      this.parameters = parameters;
    }

  }
}
