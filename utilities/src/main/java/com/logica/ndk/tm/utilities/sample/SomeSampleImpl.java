package com.logica.ndk.tm.utilities.sample;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomeSampleImpl {
  private static final Logger LOG = LoggerFactory.getLogger(SomeSampleImpl.class);

  /**
   * Example pre utilitu s komplexnymi, viacerymi parametrami. Ako response nepouzivat List
   * komplexnych objektov ale list zawrapovovat do objektu tak ako je to ukazane v SampleResponse. List java native
   * objektov je funkcny (napr. List<String>).
   * 
   * @param par1
   *          Example pre komplexny parameter.
   * @param what
   *          Example pre parameter ineho typu; ak je rovny "error", metoda vrhne vynimku.
   * @return
   * @throws SampleException
   * @throws AnotherSampleException
   */
  public SampleResponse execute(SampleParam par1, String what) throws SampleException, AnotherSampleException {
    LOG.debug("Called execute {} {}", par1, what);
    final SampleResponse response = new SampleResponse();
    response.setCountry("slovensko");
    response.setCity("trnava");
    SampleListItem item = new SampleListItem("param1", new Integer(1));
    List<SampleListItem> list = new ArrayList<SampleListItem>();
    list.add(item);
    response.setList(list);
    if ("error".equals(what)) {
      LOG.warn("Throwing {}", what);
      throw new SampleException("Throwing " + what);
    }
    LOG.debug("Returning " + response);
    return response;
  }

  /** Priklad inej metody s rovnakou signaturou na demonstraciu <method-entry-point-resolver> v Mule */
  public SampleResponse executeOther(SampleParam par1, String what) throws SampleException, AnotherSampleException {
    LOG.debug("Called execute2 {} {}", par1, what);
    SampleListItem item = new SampleListItem("param1", new Integer(1));
    List<SampleListItem> list = new ArrayList<SampleListItem>();
    list.add(item);
    final SampleResponse response = new SampleResponse("other", "result", list);
    LOG.debug("Returning " + response);
    return response;
  }

}
