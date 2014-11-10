/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.task.IDTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * @author kovalcikm
 */
public class CreateIntEntitiesFromImportImplIT {
  
  static final String IMPORT_PATH = TmConfig.instance().getString("import.importDir");
  private static final String TM_USER = "svctm";
  
  CreateIntEntitiesFromImportImpl createIEFromImport;
  private static WFClient client = new WFClient();
  Task testedTask;
  File importTarget;
  java.util.List<File> expectedList = new ArrayList<File>();

  @Before
  public void setUp() throws Exception {
  
  }
  
  @After
  public void tearDown() throws IOException {
  }

  //@Ignore
  /*public void testExecute() throws BadRequestException, JsonParseException, JsonMappingException, IOException, TransformerException {
    // Setup
    expectedList = new ArrayList<File>();
    Enumerator typeEnum = new Enumerator((long)1, "PICTURES");
    IDTask task = new IDTask();
    task.setUrl("www.logica");
    task.setImportType(typeEnum);
    testedTask = client.createTask(task, TM_USER, true);
    importTarget =  new File(IMPORT_PATH + "/" + testedTask.getId() + "/");
    FileUtils.copyDirectory(
        new File("test-data/import/anl/"), 
        importTarget, FileFilterUtils.makeSVNAware(new IOFileFilter() {

      @Override
      public boolean accept(File arg0) {
        return arg0.isDirectory();
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        // TODO Auto-generated method stub
        return true;
      }}));
    File[] pom = importTarget.listFiles();
    for (File f : pom){
      if (f.isDirectory()) expectedList.add(f);
    }

    // Run
    createIEFromImport = new CreateIntEntitiesFromImportImpl();
    String response = createIEFromImport.execute(testedTask.getId(), "");

    assertEquals("OK", response);

    for (String entityId : entityIds) {
      TaskHeader header = new TaskHeader();
      header.setId(Long.valueOf(entityId));
      header.setPackageType(WFClient.PACKAGE_TYPE_IE);
      IETask entity = (IETask) client.getTask(header);
      assertEquals(testedTask.getId(), entity.getSourcePackage());
    }
    
    // Tear down
    //FileUtils.deleteDirectory(importTarget);
    
  }*/

  @Ignore
  public void testExecuteWA() throws BadRequestException, JsonParseException, JsonMappingException, IOException, TransformerException {
    // Setup
    expectedList = new ArrayList<File>();
    Enumerator typeEnum = new Enumerator((long)2, "WA");
    IDTask task = new IDTask();
    task.setUrl("www.wa.logica");
    task.setImportType(typeEnum);
    testedTask = client.createTask(task, TM_USER, true);
    importTarget =  new File(IMPORT_PATH + "/" + testedTask.getId() + "/");
    FileUtils.copyDirectory(
        new File("test-data/import/wa/"), 
        importTarget, FileFilterUtils.makeSVNAware(new IOFileFilter() {

      @Override
      public boolean accept(File arg0) {
        return true;
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        // TODO Auto-generated method stub
        return true;
      }}));
    File[] pom = importTarget.listFiles();
    for (File f : pom){
      expectedList.add(f);
    }

    // Run
    createIEFromImport = new CreateIntEntitiesFromImportImpl();
    String response = createIEFromImport.execute(testedTask.getId(), "");

    assertEquals("OK", response);

    /*for (String entityId : entityIds) {
      TaskHeader header = new TaskHeader();
      header.setId(Long.valueOf(entityId));
      header.setPackageType(WFClient.PACKAGE_TYPE_IE);
      IETask entity = (IETask) client.getTask(header);
      assertEquals(testedTask.getId(), entity.getSourcePackage());
    }*/
    
    // Tear down
    //FileUtils.deleteDirectory(importTarget);
    
  }

}
