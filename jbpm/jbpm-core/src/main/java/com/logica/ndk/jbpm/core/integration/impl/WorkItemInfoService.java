package com.logica.ndk.jbpm.core.integration.impl;

import java.util.List;

import org.drools.persistence.info.WorkItemInfo;
import org.drools.process.instance.WorkItem;

/**
 * Service to operate with WorkItemInfo.
 * 
 * @author Rudolf Daco
 */
public interface WorkItemInfoService {
  /**
   * Najde vsetky WorkItemInfo - WorkItemInfo reprezentuje WI handler ktory nie je dokonceny pretoze ak sa Wi dokonci
   * (uspesne/neuspesne) tak je z tejto DB tab odstraneny.
   * 
   * @return
   * @throws ServiceException
   */
  public List<WorkItemInfo> findAll() throws ServiceException;

  /**
   * Najde vsetky WorkItemInfo (see findAll) a extrahuje z nich objekty WorkItem.
   * 
   * @return
   * @throws ServiceException
   */
  public List<WorkItem> findAllWorkItem() throws ServiceException;
  
  public List<WorkItemInfo> find(long processInstanceId) throws ServiceException;

  public List<WorkItem> findWorkItem(long processInstanceId) throws ServiceException;

  /**
   * Odstrani vsetky neaktualne WorkItemInfo z DB tabulky WorkItemInfo. WorkItemInfo tabulka obsahuje vsetky nedokoncene
   * WI. Potrebujeme odstranit tie ktore su neaktulane teda tie ktore su odpadom - su tam, ale proces pre nich uz je
   * ukonceny, takze su uz nepouzitelne. Tato funkcia sa musi zavolat pred resume, pretoze resume sa robi nad vsetkymi
   * WorkItemInfo. Taketo nedokoncene wi mozu vznikat ak synch handler hodi exception ale nestihne sa zaperzistovat ze
   * tento wi skoncil.
   * 
   * @throws ServiceException
   */
  public void deleteGarbage() throws ServiceException;
}
