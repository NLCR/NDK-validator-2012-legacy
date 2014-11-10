package com.logica.ndk.tm.utilities.sample;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(targetNamespace = "http://wwww.logica.com/ndk/tm/process")
public interface SomeSample {

  /**
   * Example pre utilitu s komplexnymi, viacerymi parametrami. Synchronna verzia. Ako response nepouzivat List
   * komplexnych objektov ale list zawrapovovat do objektu tak ako je to ukazane v SampleResponse. List java native
   * objektov je funkcny (napr. List<String>).
   * 
   * @param parameterOne
   *          Example pre komplexny parameter.
   * @param what
   *          Example pre parameter ineho typu; ak je rovny "error", metoda vrhne vynimku SampleException.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod
  public SampleResponse execute(@WebParam(name = "par1") SampleParam par1, @WebParam(name = "what") String what) throws SampleException, AnotherSampleException;

  /**
   * Example pre utilitu s komplexnymi, viacerymi parametrami. Asynchronna verzia.
   * 
   * @param parameterOne
   *          Example pre komplexny parameter.
   * @param parameterTwo
   *          Example pre parameter ineho typu; ak je rovny "error", metoda vrhne vynimku SampleException.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  @WebMethod(operationName = "executeAsync")
  public void executeAsynchronous(@WebParam(name = "par1") SampleParam par1, @WebParam(name = "parameterTwo") String what) throws SampleException, AnotherSampleException;

}
