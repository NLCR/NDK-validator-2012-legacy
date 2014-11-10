package com.logica.ndk.tm.utilities.integration.aleph;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

public class CreateAlephEnrichedDataImplIT {

  @Ignore
	public void testBarCode() throws Exception {
		CreateAlephEnrichedDataImpl alephData = new CreateAlephEnrichedDataImpl();
		String response = alephData.createBibliographicEnrichedDataByBarCode("1312778-10", "", GetAlephDataImpl.LIBRARY_NK, null, "abc", true); 
		assertTrue(response.contains("Der Archipel Gulag"));
		assertTrue(response.contains("<subfield label=\"b\">T 114447</subfield>"));
	}

}
