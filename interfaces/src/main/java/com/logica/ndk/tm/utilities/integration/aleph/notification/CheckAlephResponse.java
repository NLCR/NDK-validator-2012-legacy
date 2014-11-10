package com.logica.ndk.tm.utilities.integration.aleph.notification;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Kontroluje stav Alpeh notification response v danom CDM. Aleph uz odpovedal a odoslal odpoved a odpoved uz bola
 * prekopirovana do spravneho CDM (urobila to utilita ReadAlephResponseImpl). Tato utilta uz len kontroluje odpoved v
 * danom CDM.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface CheckAlephResponse {

  /**
   * Skontroluje ci uz existuje v CDM subor s Aleph notification response. Ak ano, odpoved sa precita a ak je odpovede
   * OK tak return CHECK_ALEPH_RESPONSE_STATUS_OK ak odpoved je ERROR tak exception ak este subor neexistuje tak return
   * CHECK_ALEPH_RESPONSE_STATUS_WAIT.
   * 
   * @param cdmId
   * @return
   * @throws AlephNotificationException
   */
  @WebMethod
  public String executeSync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "locality") String locality) throws AlephNotificationException, BusinessException, SystemException;

  /**
   * @param cdmId
   * @throws AlephNotificationException
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "cdmId") String cdmId, @WebParam(name = "locality") String locality) throws AlephNotificationException, BusinessException, SystemException;
}
