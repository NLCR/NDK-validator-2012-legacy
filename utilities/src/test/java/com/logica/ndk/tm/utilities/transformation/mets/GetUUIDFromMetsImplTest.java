package com.logica.ndk.tm.utilities.transformation.mets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GetUUIDFromMetsImplTest {

  /* Not using inner uuid
  @Test
  public void testExecute() {
    GetUUIDFromMetsImpl u = new GetUUIDFromMetsImpl();
    String uuid = u.execute("test-data\\import\\anl\\ANL000001\\METS_ANL000001.xml");
    assertEquals("CF131E10-B592-45C0-B915-750270A79BF9", uuid);
  }

  @Test(expected = METSPasrsingFailedException.class)
  public void testExecuteMissingFile() {
    GetUUIDFromMetsImpl u = new GetUUIDFromMetsImpl();
    u.execute("test-data\\import\\anl\\ANL000001\\METS_ANL000002.xml");
  }

  @Test(expected = ElementNotFoundException.class)
  public void testExecuteMissingUUID() {
    GetUUIDFromMetsImpl u = new GetUUIDFromMetsImpl();
    u.execute("test-data\\import\\anl\\METS_ANL000001_missing_uuid.xml");
  }
  */
  
  @Test
  public void testExecute() {
    GetUUIDFromMetsImpl u = new GetUUIDFromMetsImpl();
    String uuid = u.execute("test-data\\import\\anl\\ANL000001\\METS_ANL000001.xml");
    assertEquals("ANL000001", uuid);
  }
}
