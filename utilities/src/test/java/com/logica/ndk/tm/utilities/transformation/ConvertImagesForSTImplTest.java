package com.logica.ndk.tm.utilities.transformation;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.CDMUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

@Ignore
public class ConvertImagesForSTImplTest extends CDMUtilityTest {

	 private static final String CMD_ID_TIFFINFO_RGB300 = "tiffinfo_rgb300";
	 
	 private final CDM cdm = new CDM();
	 
	 ConvertImagesForSTImpl convertImg;

	  
	 @Ignore
	public void testConvertImages() throws Exception {
		setUpCdmById(CMD_ID_TIFFINFO_RGB300);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_RGB300).getAbsolutePath();		
		
		convertImg = new ConvertImagesForSTImpl();
		String result = convertImg.execute(flatDataDirName, CMD_ID_TIFFINFO_RGB300);
		assertThat(result).isEqualTo(ResponseStatus.RESPONSE_OK);
		deleteCdmById(CMD_ID_TIFFINFO_RGB300);		
	}

}
