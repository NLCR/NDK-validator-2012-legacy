package com.logica.ndk.tm.utilities.integration.wf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.exception.BadRequestException;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedPackageTask;
import com.logica.ndk.tm.utilities.integration.wf.finishedTask.FinishedTask;
import com.logica.ndk.tm.utilities.integration.wf.task.IETask;
import com.logica.ndk.tm.utilities.integration.wf.task.PackageTask;
import com.logica.ndk.tm.utilities.integration.wf.task.Task;
import com.logica.ndk.tm.utilities.integration.wf.task.TaskHeader;
import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

public class CreateIntEntitiesImplIT {

  private static final String TM_USER = "majdaf";
  private static WFClient client = new WFClient();
  Task testedTask;

  @Before
  public void setUp() throws Exception {
    String barCode = "1000748306";

    // Create new task (PREPARE)
    PackageTask task = new PackageTask();
    task.setBarCode(barCode);
    task.setDocumentLocality(new DocumentLocality("NKCR", "NKCR", "NKCR"));
    testedTask = client.createTask(task, TM_USER, true);

    // Finish task (PREPARE)
    FinishedTask finishedTask = new FinishedPackageTask();
    finishedTask.setId(testedTask.getId());
    finishedTask.setUser(TM_USER);
    client.signalFinishedTask(finishedTask, "NDKSigDigitFinish");

    TaskHeader taskHeader = new TaskHeader(testedTask);
    int counter = 0;
    while (!testedTask.getActivity().getCode().equals("SCAN")) {
      counter++;
      if (counter > 20) {
        fail("CREATEDIR timeout");
      }
      testedTask = client.getTask(taskHeader);
      System.out.println(counter);
      System.out.println(testedTask.getActivity().getCode());
      System.out.println(testedTask.getReservedInternalId());
      Thread.sleep(1000);
    }
  }

  @After
  public void tearDown() throws Exception {
  }

  @Ignore
  public void testExecute() throws BadRequestException, JsonParseException, JsonMappingException, IOException, TransformerException {
    CreateIntEntitiesImpl u = new CreateIntEntitiesImpl();
    String cdmId = ((PackageTask) testedTask).getPathId();
    List<String> childCdmIds = new ArrayList<String>();
    childCdmIds.add(cdmId + "_1");
    childCdmIds.add(cdmId + "_2");
    childCdmIds.add(cdmId + "_3");

    List<String> entityIds = u.execute(testedTask.getId(), cdmId, childCdmIds);

    assertNotNull(entityIds);
    assertTrue(entityIds.size() == 3);

    int i = 0;
    for (String entityId : entityIds) {
      TaskHeader header = new TaskHeader();
      header.setId(Long.valueOf(entityId));
      header.setPackageType(WFClient.PACKAGE_TYPE_IE);
      IETask entity = (IETask) client.getTask(header);
      assertEquals(testedTask.getId(), entity.getSourcePackage());
      assertEquals(childCdmIds.get(i), entity.getPathId());
      i++;
    }

  }

}
