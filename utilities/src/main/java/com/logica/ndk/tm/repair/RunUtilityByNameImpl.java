/**
 * 
 */
package com.logica.ndk.tm.repair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.logica.ndk.tm.utilities.AbstractUtility;

/**
 * @author kovalcikm
 */
public class RunUtilityByNameImpl extends AbstractUtility {

  public void execute(String utilityQualifiedName, String methodName, String[] params) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    log.info("Going to run utility: " + utilityQualifiedName);
    log.info("Method name:" + methodName);
    log.info("Utility parameters: " + params);
    
    Class<?> clazz = Class.forName(utilityQualifiedName, false, this.getClass().getClassLoader());
    Constructor<?> ctor = clazz.getConstructor();
    Object object = ctor.newInstance(new Object[] {});
    Method[] methods = clazz.getMethods();

    for (Method method : methods) {
      if (method.getName().equals(methodName)) {
        method.invoke(object, (Object[]) params);
      }
    }
    
    log.info("Utility execution finished.");
  }
  
  
  public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    String[] params = new String[1];
    params[0] = "cdmIdValue";
    new RunUtilityByNameImpl().execute("com.logica.ndk.tm.utilities.premis.GeneratePremisImpl", "execute", params);
  }
}



