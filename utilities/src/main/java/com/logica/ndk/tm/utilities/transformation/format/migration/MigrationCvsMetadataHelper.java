package com.logica.ndk.tm.utilities.transformation.format.migration;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;

/**
 * @author brizat
 *
 */
public class MigrationCvsMetadataHelper {

	private static final Logger log = LoggerFactory.getLogger(MigrationCvsMetadataHelper.class);
	private static final String DEFAULT_BARCODE = "default";
	private static final String METADATA_FILE_NAME = TmConfig.instance().getString("format-migration.metadata-file");
	
	public static char CSV_COLUMN_DELIMITER = ';';
	public static char CSV_TEXT_QUALIFIER = '"';
	
	private static final List<MethodInvocation> MAPPING_METHODS = loadDefinitions();
	private File csvFile ;
	
	public MigrationCvsMetadataHelper(File csvFile){
		this.csvFile = csvFile;		
	}
	
	public MigrationCvsMetadataHelper(){
  }
	
	private static List<MethodInvocation> loadDefinitions(){
		List<MethodInvocation> result = new LinkedList<MethodInvocation>();
		
		for(Method method : PackageMetadataBuilder.class.getDeclaredMethods()){
			if(method.isAnnotationPresent(FromCsvColumn.class)){
				FromCsvColumn annotation = method.getAnnotation(FromCsvColumn.class);
				result.add(new MethodInvocation(annotation.columnName(), method, annotation.mandatory(), annotation.defaultValue()));
			}
		}
		return result;
	}
	
	public Map<String, List<PackageMetadata>> load() throws ReadCsvFileException{
		return load(csvFile);
	}	
	
	public Map<String, List<PackageMetadata>> load(File csvFile) throws ReadCsvFileException{
		Map<String, List<PackageMetadata>> result = new HashMap<String, List<PackageMetadata>>();
		CsvReader reader = getReader(csvFile);
		StringBuilder mandatoryFilesMissings = new StringBuilder();
		try {
			reader.readHeaders();
			while(reader.readRecord()){
				PackageMetadataBuilder packageMetadataBuilder = new PackageMetadataBuilder();
				for (MethodInvocation methodInvocation : MAPPING_METHODS) {
					String parameterValue = reader.get(methodInvocation.getColumnName());
					boolean invoke = true;
					if(parameterValue == null || parameterValue.isEmpty()){
					    if(!methodInvocation.getDefaultValue().isEmpty()){
					      parameterValue = methodInvocation.getDefaultValue();
					    }else if(methodInvocation.isMandatory()){
					      mandatoryFilesMissings.append("Mandatory value from column ").append(methodInvocation.getColumnName()).append(" missing").append("\r\n");
					      invoke = false;
					    }
					}
					
					if(invoke){
					  methodInvocation.getInvocationMethod().invoke(packageMetadataBuilder, parameterValue);
					}					
				}
				PackageMetadata packageMetadata = packageMetadataBuilder.build();
				if(result.containsKey(packageMetadata.getBarcode())){
				  result.get(packageMetadata.getBarcode()).add(packageMetadata);
				}else{
				  List<PackageMetadata> metadata = new LinkedList<PackageMetadata>();
				  metadata.add(packageMetadata);
				  result.put(packageMetadata.getBarcode(), metadata);
				}
			}
		}catch (IOException e) {
			log.error("Could not read data from input file", e);
			throw new ReadCsvFileException("Could not read data from input file", e);
		}catch (InvocationTargetException ex){
			log.error("Could not read data from input file", ex);
			throw new ReadCsvFileException("Could not read data from input file", ex);
		}catch (IllegalAccessException ex){
			log.error("Could not read data from input file", ex);
			throw new ReadCsvFileException("Could not read data from input file", ex);
		}
		
		
		if(mandatoryFilesMissings.length() > 0){
			throw new ReadCsvFileException(mandatoryFilesMissings.toString());
		}
		
		return result;
	}

	
	private CsvReader getReader(File csvFile){
	    CsvReader reader = null;
	    try {
	      //reader = new CsvReader(inFile);
	      reader = new CsvReader(new FileInputStream(csvFile), Charset.forName("UTF-8"));
	      //reader = new CsvReader(new InputStreamReader(new FileInputStream(inFile)), "UTF-8");
	      reader.setDelimiter(CSV_COLUMN_DELIMITER);
	      reader.setTextQualifier(CSV_TEXT_QUALIFIER);
	      reader.setUseComments(true);

	      return reader;
	    }
	    catch (final FileNotFoundException e) {
	      throw new SystemException(format("Read file %s failed", csvFile.getAbsolutePath()), ErrorCodes.CSV_READING);
	    }
	}
	
	public Map<String, List<PackageMetadata>> updatePackageMetadataFromDefault(Map<String, List<PackageMetadata>> orgMetadata) {
    List<PackageMetadata> metadatas = orgMetadata.get(DEFAULT_BARCODE);
    if (metadatas.size() != 1) {
      throw new BusinessException("Could not find default values in " + METADATA_FILE_NAME);
    }
    PackageMetadata defaultValues = metadatas.get(0);
    
    for (List<PackageMetadata> metadatasToUpdate : orgMetadata.values()) {
      for (PackageMetadata toUpdate : metadatasToUpdate) {
        if(toUpdate.getAlephCode() == null || toUpdate.getAlephCode().isEmpty()){
          toUpdate.setAlephCode(defaultValues.getAlephCode());
        }
        if(toUpdate.getAlephLocation() == null || toUpdate.getAlephLocation().isEmpty()){
          toUpdate.setAlephLocation(defaultValues.getAlephLocation());
        }
        if(toUpdate.getIssueNumber() == null || toUpdate.getIssueNumber().isEmpty()){
          toUpdate.setIssueNumber(defaultValues.getIssueNumber());
        }
        if(toUpdate.getLocalityCode() == null|| toUpdate.getLocalityCode().isEmpty()){
          toUpdate.setLocalityCode(defaultValues.getLocalityCode());
        }
        if(toUpdate.getPageType() == null|| toUpdate.getPageType().isEmpty()){
          toUpdate.setPageType(defaultValues.getPageType());
        }
        if(toUpdate.getTemplate() == null|| toUpdate.getTemplate().isEmpty()){
          toUpdate.setTemplate(defaultValues.getTemplate());
        }
        if(toUpdate.isRename() == null){
          toUpdate.setRename(defaultValues.isRename());
        }
        updateMetadata(toUpdate.getEnvBeanColor(), defaultValues.getEnvBeanColor());
        updateMetadata(toUpdate.getEnvBeanGrayscale(), defaultValues.getEnvBeanGrayscale());
      }
    }

    return orgMetadata;
  }

  private void updateMetadata(MixEnvBean toUpdateBean, MixEnvBean defaultValues) {
    //TODO - Stupid if else, should be more intelligent(reflection)
    if (toUpdateBean.getCaptureDevice() == null || toUpdateBean.getCaptureDevice().isEmpty()) {
      toUpdateBean.setCaptureDevice(defaultValues.getCaptureDevice());
    }

    if (toUpdateBean.getFormatName() == null || toUpdateBean.getFormatName().isEmpty()) {
      toUpdateBean.setFormatName(defaultValues.getFormatName());
    }

    if (toUpdateBean.getFormatVersion() == null || toUpdateBean.getFormatVersion().isEmpty()) {
      toUpdateBean.setFormatVersion(defaultValues.getFormatVersion());
    }

    if (toUpdateBean.getImageProducer() == null || toUpdateBean.getImageProducer().isEmpty()) {
      toUpdateBean.setImageProducer(defaultValues.getImageProducer());
    }

    if (toUpdateBean.getOpticalResolutionUnit() == null || toUpdateBean.getOpticalResolutionUnit().isEmpty()) {
      toUpdateBean.setOpticalResolutionUnit(defaultValues.getOpticalResolutionUnit());
    }

    if (toUpdateBean.getScannerManufacturer() == null || toUpdateBean.getScannerManufacturer().isEmpty()) {
      toUpdateBean.setScannerManufacturer(defaultValues.getScannerManufacturer());
    }

    if (toUpdateBean.getScannerModelName() == null || toUpdateBean.getScannerModelName().isEmpty()) {
      toUpdateBean.setScannerModelName(defaultValues.getScannerModelName());
    }

    if (toUpdateBean.getScannerModelNumber() == null || toUpdateBean.getScannerModelNumber().isEmpty()) {
      toUpdateBean.setScannerModelNumber(defaultValues.getScannerModelNumber());
    }

    if (toUpdateBean.getScannerModelSerialNo() == null || toUpdateBean.getScannerModelSerialNo().isEmpty()) {
      toUpdateBean.setScannerModelSerialNo(defaultValues.getScannerModelSerialNo());
    }

    if (toUpdateBean.getScannerSensor() == null || toUpdateBean.getScannerSensor().isEmpty()) {
      toUpdateBean.setScannerSensor(defaultValues.getScannerSensor());
    }

    if (toUpdateBean.getScanningSoftwareName() == null || toUpdateBean.getScanningSoftwareName().isEmpty()) {
      toUpdateBean.setScanningSoftwareName(defaultValues.getScanningSoftwareName());
    }

    if (toUpdateBean.getScanningSoftwareVersionNo() == null || toUpdateBean.getScanningSoftwareVersionNo().isEmpty()) {
      toUpdateBean.setScanningSoftwareVersionNo(defaultValues.getScanningSoftwareVersionNo());
    }

    if (toUpdateBean.getxOpticalResolution() == null || toUpdateBean.getxOpticalResolution().isEmpty()) {
      toUpdateBean.setxOpticalResolution(defaultValues.getxOpticalResolution());
    }

    if (toUpdateBean.getyOpticalResolution() == null || toUpdateBean.getyOpticalResolution().isEmpty()) {
      toUpdateBean.setyOpticalResolution(defaultValues.getyOpticalResolution());
    }
  }
	
	public static void main(String[] args) throws ReadCsvFileException {
		File file = new File("D:\\work\\ndk\\test_data\\import-properties.csv");
		Map<String, List<PackageMetadata>> load = new MigrationCvsMetadataHelper().load(file);
		new MigrationCvsMetadataHelper().updatePackageMetadataFromDefault(load);
		new PackageMetadataValidator().validate(load.get("1002726448").get(0));
		load.size();
	}
	
}
