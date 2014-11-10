/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author kovalcikm
 *
 */ 

public class UpdateFoxmlMetadataImplTest {

  //TODO implement test
  
  @Test
  public void test(){
    List<String> partsToUpdate = new ArrayList<String>();
    partsToUpdate.add("mods");
    partsToUpdate.add("dc");
    new UpdateFoxmlMetadataImpl().execute("61fa4d60-b4de-11e3-8ba6-00505682629d", partsToUpdate, "nkcr", "aaa.csv", true);
  }
  
}
