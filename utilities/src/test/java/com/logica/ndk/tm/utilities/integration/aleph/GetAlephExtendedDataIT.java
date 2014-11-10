package com.logica.ndk.tm.utilities.integration.aleph;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;


public class GetAlephExtendedDataIT {
  @Ignore
	public void testBarCode() throws Exception {
	  GetAlephExtendedDataImpl utBarCode = new GetAlephExtendedDataImpl();
//		GetAlephExtendedDataResponse response = utBarCode.getBibliographicDataByBarCode("1312778-10", GetAlephDataImpl.LIBRARY_NK, null); 
//		assertTrue(response.getResult().contains("Reforms of Secondary Schools in Czechoslovakia"));
//		assertTrue("000934119".equals(response.getDocnum()));
	}

  @Ignore//(expected = ItemNotFoundException.class)
	public void testBarCode2() throws Exception {
	  GetAlephExtendedDataImpl utBarCode = new GetAlephExtendedDataImpl();
		utBarCode.getBibliographicDataByBarCode("ahoj7006",GetAlephDataImpl.LIBRARY_NK, GetAlephDataImpl.ALEPH_BASE_NK_MAIN); 
	}

	@Test(expected = AlephUnaccessibleException.class)
	public void testBarCode3() throws Exception {
	  GetAlephExtendedDataImpl utBarCode = new GetAlephExtendedDataImpl();
	  utBarCode.getBibliographicDataByBarCode("1000957006", "Mars", GetAlephDataImpl.ALEPH_BASE_NK_MAIN);
	}
}
