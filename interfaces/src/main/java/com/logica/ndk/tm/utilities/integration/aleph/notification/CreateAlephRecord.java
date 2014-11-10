package com.logica.ndk.tm.utilities.integration.aleph.notification;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Utilita na vytvorenie Aleph notification request pre dane CDM. Kazde CDM pocas spracovania by malo pouzit tuto
 * utilitu. Vytvoreny request sa kopiruje do spolocneho centralneho adresara ktory potom sleduje utilita
 * NotifyAlephImpl. Utilita NotifyAlephImpl pouziva pocas spracovania koncept presunu suborov medzi pracovnymi sub-
 * adresarmi a preto aj tato utilta presuva subor do sub-adresa incoming v centralnom adresari.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CreateAlephRecord {

  /**
   * Returns OK or throws expcetion.
   * 
   * @param cdmId
   * @param docNumbe
   * @return
   * @throws AlephNotificationException
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "docNumber") String docNumber, @WebParam(name = "locality") String locality) throws AlephNotificationException, BusinessException, SystemException;

  /**
   * @param cdmId
   * @param docNumbe
   * @throws AlephNotificationException
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "docNumber") String docNumber, @WebParam(name = "locality") String locality) throws AlephNotificationException, BusinessException, SystemException;
}
