/**
 * 
 */
package com.logica.ndk.tm.utilities.io;

import org.junit.Test;

import com.logica.ndk.tm.utilities.CDMUtilityTest;

/**
 * @author kovalcikm
 *
 */
public class DeleteDirImplTest extends CDMUtilityTest{
  

  @Test
  public void test(){
    
    new DeleteDirImpl().execute("a2af9790-3e18-11e2-aa71-00505682629d", "backup", true);
  }
  
}
