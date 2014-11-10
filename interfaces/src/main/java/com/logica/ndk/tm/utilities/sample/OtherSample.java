package com.logica.ndk.tm.utilities.sample;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface OtherSample {

  /**
   * Example pre utilitu s jednym jednoduchym parametrom. Synchronna verzia.
   * 
   * @param par1
   *          Example pre parameter; ak je rovny "error", metoda vrhne vynimku.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod
  public String executeSync(@WebParam(name = "par1") String par1) throws SampleException, AnotherSampleException;

  /**
   * Example pre utilitu s jednym jednoduchym parametrom. Synchronna verzia.
   * 
   * @param par1
   *          Example pre parameter; ak je rovny "error", metoda vrhne vynimku.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod
  public void executeAsync(@WebParam(name = "par1") String par1) throws SampleException, AnotherSampleException;

}
