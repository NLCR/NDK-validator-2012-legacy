package com.logica.ndk.tm.utilities.transformation.em.getuuid.cdm;

import org.dom4j.Node;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;

/**
 * User: krchnacekm
 */
public final class UUIDMetsService {

  private static final String PATH_TO_UUID = "//mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods[@ID=\"%s\"]/mods:identifier[@type=\"uuid\"]/text()";
  private static final String PATH_TO_TITLE = "//mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title";
  private static final String MODS_TITLE_ID = "MODS_TITLE_0001";
  private static final String MODS_VOLUME_ID = "MODS_VOLUME_0001";
  private final CDM cdm;
  private final CDMMetsHelper cdmMetsHelper;

  public UUIDMetsService(CDM cdm, CDMMetsHelper cdmMetsHelper) {
    this.cdm = cdm;
    this.cdmMetsHelper = cdmMetsHelper;
  }

  /**
   * @param cdmId
   * @param uuidResult
   *          Method can change value in this argument !!!
   */
  public void findTitleInMets(final String cdmId, final UUIDResult uuidResult) {
    final Node nodeTitleFromMets = this.cdmMetsHelper.getNodeFromMets(PATH_TO_TITLE, this.cdm, cdmId);
    if (nodeTitleFromMets != null) {
      uuidResult.setTitle(nodeTitleFromMets.getText());
    }
  }

  /**
   * @param cdmId
   * @param uuidResult
   *          Method can change value in this argument !!!
   */
  public void findVolumeUUIDInMets(final String cdmId, final UUIDResult uuidResult) {
    final Node nodeVolumeUUIDFromMets = this.cdmMetsHelper.getNodeFromMets(String.format(PATH_TO_UUID, MODS_VOLUME_ID), this.cdm, cdmId);
    if (nodeVolumeUUIDFromMets != null) {
      uuidResult.setVolumeUUID(nodeVolumeUUIDFromMets.getText());
    }
  }

  /**
   * @param cdmId
   * @param uuidResult
   *          Method can change value in this argument !!!
   */
  public void findTitleUUIDInMets(final String cdmId, final UUIDResult uuidResult) {
    final Node nodeTitleUUIDFromMets = this.cdmMetsHelper.getNodeFromMets(String.format(PATH_TO_UUID, MODS_TITLE_ID), this.cdm, cdmId);
    if (nodeTitleUUIDFromMets != null) {
      uuidResult.setTitleUUID(nodeTitleUUIDFromMets.getText());
    }
  }

  /**
   * Method compares volume number in input of GetUUID utility and volume part number in mets.
   * 
   * @param cdmId
   * @param volumeNumber
   * @return True if values are equals or false otherwise.
   */
  public Boolean checkIfArePartNumberAndVolumeNumberEquals(String cdmId, String volumeNumber) {
    if (cdmId != null && volumeNumber != null) {
      final String volumeNumberFromMets = new CDMMetsHelper().getVolumeNumber(cdm, cdmId);
      return volumeNumber.equals(volumeNumberFromMets);
    }
    else {
      throw new IllegalArgumentException(String.format("Arguments cdmId and volumeNumber are mandatory. (cdmId: %s, volumeNumber: %s)", cdmId, volumeNumber));
    }
  }

  /**
   * Find volume number in mets file by cdmId.
   * 
   * @param cdmId
   * @return Volume number or empty string
   */
  public String findVolumeNumber(String cdmId) {
    if (cdmId != null) {
      return new CDMMetsHelper().getVolumeNumber(cdm, cdmId);
    }
    else {
      throw new IllegalArgumentException("Argument cdmId is mandatory.");
    }
  }
}
