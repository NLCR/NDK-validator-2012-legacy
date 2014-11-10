package com.logica.ndk.tm.utilities.validation;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.CDMUtilityTest;

@Ignore
public class ValidateTiffinfoImplTest extends CDMUtilityTest {

	 private static final String CMD_ID_TIFFINFO_BW300 = "tiffinfo_bw300";
	 private static final String CMD_ID_TIFFINFO_GRAY300 = "tiffinfo_gray300";
	 private static final String CMD_ID_TIFFINFO_RGB300 = "tiffinfo_rgb300";
	 private static final String CMD_ID_TIFFINFO_RGB200 = "tiffinfo_rgb200";
	 private static final String CMD_ID_TIFFINFO_RGB200_LZW = "tiffinfo_rgb200-lzw";
	 private static final String CMD_ID_TIFFINFO_RGB300_JPG = "tiffinfo_rgb300-jpg";
	 
	 private final CDM cdm = new CDM();
	 

	ValidateTiffinfoImpl validatorTiffinfo;
	
	@Ignore
	public void testValidateColor() throws Exception {
		setUpCdmById(CMD_ID_TIFFINFO_RGB300);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_RGB300).getAbsolutePath();		
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_RGB300).getAbsolutePath();
		
		validatorTiffinfo= new ValidateTiffinfoImpl();
		ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
				ValidateTiffinfoImpl.COLOR_MODE_RGB, "300", "300", cdmDataDir, false);
		assertThat(response.getViolationsList()).isEmpty();
		deleteCdmById(CMD_ID_TIFFINFO_RGB300);		
	}

	@Ignore 
	public void testValidateBW() throws Exception {
		setUpCdmById(CMD_ID_TIFFINFO_BW300);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_BW300).getAbsolutePath();	
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_BW300).getAbsolutePath();

		validatorTiffinfo= new ValidateTiffinfoImpl();
		ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
				ValidateTiffinfoImpl.COLOR_MODE_BW, "300", "300", cdmDataDir, false);
		assertThat(response.getViolationsList()).isEmpty();
		deleteCdmById(CMD_ID_TIFFINFO_BW300);
	}
	
	@Ignore
	public void testValidateGrayscale() throws Exception {
		setUpCdmById(CMD_ID_TIFFINFO_GRAY300);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_GRAY300).getAbsolutePath();
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_GRAY300).getAbsolutePath();
		
		validatorTiffinfo= new ValidateTiffinfoImpl();
		ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
				ValidateTiffinfoImpl.COLOR_MODE_GRAYSCALE, "300", "300", cdmDataDir, false);
		assertThat(response.getViolationsList()).isEmpty();
		deleteCdmById(CMD_ID_TIFFINFO_GRAY300);		
	}


	@Ignore
	public void testValidateColor200dpi() throws Exception {
		setUpCdmById(CMD_ID_TIFFINFO_RGB200);		
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_RGB200).getAbsolutePath();
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_RGB200).getAbsolutePath();
		
		validatorTiffinfo= new ValidateTiffinfoImpl();
		ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
				ValidateTiffinfoImpl.COLOR_MODE_RGB, "200", "200", cdmDataDir, false);
		assertThat(response.getViolationsList()).isEmpty();
		deleteCdmById(CMD_ID_TIFFINFO_RGB200);		
	}

	@Ignore //(expected=BusinessException.class)
	public void testValidateCompressedLZWImage() throws Exception{
		setUpCdmById(CMD_ID_TIFFINFO_RGB200_LZW);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_RGB200_LZW).getAbsolutePath();
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_RGB200_LZW).getAbsolutePath();
		
		try {
			validatorTiffinfo= new ValidateTiffinfoImpl();
			ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
					ValidateTiffinfoImpl.COLOR_MODE_RGB, "200", "200", cdmDataDir, true);
			assertThat(response.getViolationsList()).hasSize(1);
		} finally {
			deleteCdmById(CMD_ID_TIFFINFO_RGB200_LZW);	
		}
	}

	@Ignore //(expected=BusinessException.class)
	public void testValidateCompressedJPGImage() throws Exception{
		setUpCdmById(CMD_ID_TIFFINFO_RGB300_JPG);
		String flatDataDirName = cdm.getFlatDataDir(CMD_ID_TIFFINFO_RGB300_JPG).getAbsolutePath();
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_RGB300_JPG).getAbsolutePath();

		try {
			validatorTiffinfo= new ValidateTiffinfoImpl();
			ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
					ValidateTiffinfoImpl.COLOR_MODE_RGB, "300", "300", cdmDataDir, true);
			assertThat(response.getViolationsList()).hasSize(1);
		} finally {
			deleteCdmById(CMD_ID_TIFFINFO_RGB300_JPG);		
		}
	}

	@Ignore //(expected=BusinessException.class)
	public void testValidateWrongDPI() throws Exception{
		setUpCdmById(CMD_ID_TIFFINFO_RGB300);
		String flatDataDirName = new CDM().getFlatDataDir(CMD_ID_TIFFINFO_RGB300).getAbsolutePath();
		String cdmDataDir = cdm.getCdmDataDir(CMD_ID_TIFFINFO_RGB300).getAbsolutePath();

		try {
			validatorTiffinfo= new ValidateTiffinfoImpl();
			ValidationViolationsWrapper response = validatorTiffinfo.execute(flatDataDirName, 
					ValidateTiffinfoImpl.COLOR_MODE_RGB, "300", "200", cdmDataDir, true);
			assertThat(response.getViolationsList()).hasSize(1);
		} finally {
			deleteCdmById(CMD_ID_TIFFINFO_RGB300);
		}
	}

}