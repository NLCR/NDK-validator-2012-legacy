package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.logica.ndk.tm.utilities.jhove.MixEnvBean;

/**
 * @author brizat
 *
 */
public class PackageMetadataValidator {

  private static String MISSING_VALUE_ERROR_MSG = "%s missing value for field %s";

  public List<String> validate(PackageMetadata metadata) {
    List<String> result = new LinkedList<String>();
    
    validateRec(metadata, "", metadata.getBarcode(), result);
    
    return result;
  }

  private void validateRec(Object object, String fieldPrefix, String barcode, List<String> results) {
    Field[] declaredFields = object.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      field.setAccessible(true);
      Object fieldValue;
      try {
        fieldValue = field.get(object);
      }
      catch (Exception e) {
        results.add(String.format(MISSING_VALUE_ERROR_MSG, barcode, fieldPrefix + field.getName()));
        continue;
      }
      
      if (fieldValue == null) {
        if(!field.isAnnotationPresent(Nullable.class)){
          results.add(String.format(MISSING_VALUE_ERROR_MSG, barcode, fieldPrefix + field.getName()));
        }
      }
      if(fieldValue instanceof String){
        if(((String)fieldValue).isEmpty()){
          if(!field.isAnnotationPresent(Nullable.class)){
            results.add(String.format(MISSING_VALUE_ERROR_MSG, barcode, fieldPrefix + field.getName()));
          }  
        }
      }
      if (fieldValue instanceof MixEnvBean) {
        validateRec(fieldValue, field.getName() + "_", barcode, results);
      }
    }
  }

}
