package com.logica.ndk.tm.utilities.integration.aleph;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;


public class GetAlephDataIT {
  @Ignore
	public void testBarCode() throws Exception {
		GetAlephDataImpl utBarCode = new GetAlephDataImpl();
		PresentResult result = utBarCode.getBibliographicDataByBarCode("1000957006", " ", GetAlephDataImpl.LIBRARY_NK, null); 
		assertTrue(result.getOAIMARC().contains("Reforms of Secondary Schools in Czechoslovakia"));
	}

  @Ignore//(expected = ItemNotFoundException.class)
	public void testBarCode2() throws Exception {
		GetAlephDataImpl utBarCode = new GetAlephDataImpl();
		utBarCode.getBibliographicDataByBarCode("ahoj7006", "", GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN); 
	}

	@Test(expected = AlephUnaccessibleException.class)
	public void testBarCode3() throws Exception {
		GetAlephDataImpl utBarCode = new GetAlephDataImpl();
		utBarCode.getBibliographicDataByBarCode("1000957006", "", "Mars", GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
	}
}
