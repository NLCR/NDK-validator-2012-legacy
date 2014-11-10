/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFilter;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.transformation.sip2.FedoraHelper;

/**
 * @author kovalcikm
 *         Sends updated FOXMLs from CDM to Fedora
 */
public class SendFoxmlToFedoraImpl extends AbstractUtility {

  public String execute(String cdmId, String locality) {
    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(locality);
    log.info("Utility SendFoxmlToFedoraImpl started for cdmId: " + cdmId);

    File updatedFoxmlDir = new File(cdm.getSIP2Dir(cdmId).getAbsolutePath() + File.separator + locality + File.separator + "xml" + File.separator + "updated");
    log.debug("Updated foxmls dir: " + updatedFoxmlDir);
    List<String> wildcardList = new ArrayList<String>();

    wildcardList.add("*.xml");
    WildcardFilter wildcardFilter = new WildcardFilter(wildcardList);
    Collection<File> updatedFoxmls = FileUtils.listFiles(updatedFoxmlDir, wildcardFilter, FileFilterUtils.falseFileFilter());

    FedoraHelper fedoraHelper = new FedoraHelper(locality,cdmId);
    for (File file : updatedFoxmls) {
      String pid = "uuid:" + FilenameUtils.getBaseName(file.getName());
      fedoraHelper.sendFoxmToFedora(pid, file);
    }
    log.info("Utility SendFoxmlToFedoraImpl finished for cdmId: " + cdmId);
    return ResponseStatus.RESPONSE_OK;
  }

  public static void main(String[] args) {
    new SendFoxmlToFedoraImpl().execute("957d65d1-c60b-11e3-87fe-00505682629d", "nkcr");
  }
}
