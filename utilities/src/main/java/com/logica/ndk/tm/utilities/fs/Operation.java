/**
 * 
 */
package com.logica.ndk.tm.utilities.fs;

/**
 * @author kovalcikm
 *
 */
public enum Operation {
   COPY("copy"), TRANSFORM("transform"), FILE_UTILS_GET("FileUtils.getFile()"), IO_FILE("new File()");
   
   private String operationName;
   
   private Operation(String operationName){
     this.operationName = operationName;
   }
   
   public String getName(){
     return operationName;
   }
}
