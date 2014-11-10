package com.logica.ndk.tm.utilities.sample;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Sample utilita pre testovacie ucely. Pouziva sa na testovanie thread pool-u pre vykonavanie urceneho poctu utilit na
 * danom Slave.
 * 
 * @author Rudolf Daco
 */
@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface SedaSample {
  /**
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod
  public String execute(@WebParam(name = "param") String param, @WebParam(name = "timeInMillis") Long timeInMillis) throws SampleException, AnotherSampleException;

  /**
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod(operationName = "executeAsync")
  public void executeAsync(@WebParam(name = "param") String param, @WebParam(name = "timeInMillis") Long timeInMillis) throws SampleException, AnotherSampleException;
}
