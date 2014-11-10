package com.logica.ndk.tm.utilities;

import org.apache.commons.id.uuid.state.ReadOnlyResourceStateEEAImpl;

/**
 * Testy ktore precuju s UUID musia extendovat tuto triedu. Pozri konstruktor tejto triedy.
 * 
 * @author ondrusekl
 */
public abstract class AbstractUtilityTest extends AbstractTest {

  protected static final String VALID_BARCODE = "1002051395";
  protected static final String VALID_ISBN = "80-204-0105-9";
  protected static final String VALID_STATE_FINISHED = "FINISHED";
  protected static final String VALID_STATE_IN_PROGRESS = "IN_PROGRESS";

  protected static final String VALID_BOOK_UUID = "uuid";

  /**
   * Nastavi lokaciu konfiguracneho suboru pre UUID pre ucely testov. UUID konfiguracny subor sa na produkcii nachadza
   * na file systeme na lokacii ktora je v tm-config. Aby ale automaticke testy mohli bezat kdekolvek aj bez tohto
   * suboru je pre ucely testu vytvoreny testovaci UUID konfigurany subor. Tento konstruktor nastavi tento testovaci
   * konfiguracny subor pre UUID generator. Testy ktore precuju s UUID musia extendovat tuto triedu.
   */
  public AbstractUtilityTest() {
    String config = System.getProperty(ReadOnlyResourceStateEEAImpl.CONFIG_FILENAME_KEY);
    if (config == null) {
      System.setProperty(ReadOnlyResourceStateEEAImpl.CONFIG_FILENAME_KEY, "uuid-test.state");
    }
  }
}
