/**
 * 
 */
package com.logica.ndk.tm.utilities.validator.versionGenerator;

import java.util.Map;
import java.util.Set;

import com.logica.ndk.tm.utilities.validator.structures.ValidationTemplate;

/**
 * @author brizat
 *
 */
public class ValidationVersionGenerator {
  
  public static String generateValidationVersion(Map<String, ValidationTemplate> validationsMade){
    StringBuilder versionBuilder = new StringBuilder();
    Set<String> validationsMadeSet = validationsMade.keySet();
    
    for(String validation: validationsMadeSet){
      ValidationTemplate validationTemplate = validationsMade.get(validation);
      if(versionBuilder.length() != 0){
        versionBuilder.append(";");
      }
      versionBuilder.append(validationTemplate.getName());
      versionBuilder.append(":");
      versionBuilder.append(validationTemplate.getVersion());      
    }
    
    return versionBuilder.toString();
  }
  
}
