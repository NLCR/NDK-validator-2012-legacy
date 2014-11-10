package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import au.edu.apsr.mtk.base.METSException;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.AssignUrnNbnResponse;
import com.logica.ndk.tm.process.UrnNbnSource;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Implementation of {@link AssignUrnNbn} WS interface.
 * 
 * @author ondrusekl
 */
public class AssignUrnNbnImpl extends UrnNbnClient {

	public AssignUrnNbnResponse assign(String registrarCode, final String cdmId) throws CDMException, DocumentException {
		if ("K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
			CDMMetsHelper metsHelper = new CDMMetsHelper();
			try {
				registrarCode = metsHelper.getDocumentSiglaForKrameriusImport(cdm, cdmId);
			} catch (Exception e) {
				log.error("Sigla not defined for Kramerius import and not found in METS file.", e);
				throw new SystemException("Sigla not defined for Kramerius import and not found in METS file.", e);
			}
		}
		assignChecks(registrarCode, cdmId);

		log.info("AssignUrnNbn started");

		File importXmlFile = cdm.getUrnXml(cdmId);
		if (!importXmlFile.exists()) {
			throw new SystemException(importXmlFile.getPath() + " does not exist.", ErrorCodes.ERROR_WHILE_READING_FILE);
		}

		Import importedDocument = null;
		try {
			JAXBContext jaxbContext = JAXBContextPool.getContext(Import.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			importedDocument = (Import) jaxbUnmarshaller.unmarshal(importXmlFile);
		} catch (JAXBException e1) {
			throw new SystemException("Error while unmarshalling " + importXmlFile.getPath(), ErrorCodes.JAXB_UNMARSHALL_ERROR);
		}
		log.debug("Document to be imported " + importedDocument);

		AssignUrnNbnResponse response = new AssignUrnNbnResponse();

		try {
			log.info("Going to post to resolver using raw http.");
			response.setUrnNbn(assignUrnNbn(registrarCode, cdmId));
			response.setUrnNbnSource(UrnNbnSource.RESOLVER);
		} catch (InaccessibleUrnNbnProviderException e) {
			response = handleInaccessibleProvider(cdmId, registrarCode, response, e);
		} catch (Exception e) { // request ended with error
			log.info("Exception in Resolver request. Status code: " + statusCode + ", Message: " + message);
			try {
				// FileUtils.write(cdm.getResolverResponseFile(cdmId),
				// "Status code: " + statusCode + "\n", true);
				retriedWrite(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
				// FileUtils.write(cdm.getResolverResponseFile(cdmId),
				// "Message: " + message + "\n", true);
				retriedWrite(cdm.getResolverResponseFile(cdmId), "Message: " + message + "\n", true);
			} catch (IOException ex) {
				log.warn("Unable to log resolver response.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
			}

			int duplicityCode = TmConfig.instance().getInt("utility.urnNbn.duplicityCode");
			if (statusCode == duplicityCode) {
				log.info("URNNBN already assigned by resolver, going to take URNNBN from METS file");
				String urnNbnFromMets;
				try {
					urnNbnFromMets = helper.getIdentifierFromMods(cdm, cdmId, URN_NBN_CODE);
				} catch (Exception e1) {
					// log.debug("Throw systemException: URNNBN identifier not founded in METS file.");
					// throw new
					// SystemException("URNNBN identifier not found in METS file.",
					// ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
					log.debug("Throw systemException: URNNBN identifier not assigned: invalid data.");
					throw new SystemException("URNNBN identifier not found in METS file.", ErrorCodes.URNNBN_NOT_ASSIGNED_INVALID_DATA);
				}

				if (urnNbnFromMets == null) {
					log.debug("Throw systemException: URNNBN identifier not founded in METS file.");
					throw new SystemException("URNNBN identifier not found in METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
				} else {
					response.setUrnNbn(urnNbnFromMets);
				}
			}
		}

		if (response.getUrnNbn() == null) {
			throw new SystemException("URNNBN not assigned.", ErrorCodes.URNNBN_RETRIEVING_ERROR);
		}
		try {
			String urnNbnBasic = helper.getIdentifierFromMods(cdm, cdmId, helper.getSectionIdMods(cdmId), URN_NBN_CODE);
			if (!isImportK4(cdmId)|| (helper.DOCUMENT_TYPE_MONOGRAPH.equals(helper.getDocumentType(cdmId)) && isImportK4(cdmId))) {
				if (urnNbnBasic == null) {
					helper.addValidUrnnbnToMods(cdmId, helper.getSectionIdMods(cdmId), response.getUrnNbn());
					helper.addIdentifierToDC(cdmId, helper.getSectionIdDC(cdmId), URN_NBN_CODE, response.getUrnNbn(), true);
				} else {
					if (!urnNbnBasic.equals(response.getUrnNbn())) {
						log.debug("Throw systemException: URNNBN identifiers from METS and from database are not equal.");
						throw new SystemException("URNNBN identifiers from METS and from database are not equal.", ErrorCodes.URNNBN_IS_DIFFERENT_FROM_METS);
					} else {
						log.info("URNNBN identifier is already in METS and URNNBN from database is the same.");
					}
				}
			}

			Node issueMODS = helper.getDmdSec(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE);
			if (issueMODS != null) {
				String urnNbnIssueMODS = helper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, URN_NBN_CODE);
				if (urnNbnIssueMODS == null) {
					helper.addValidUrnnbnToMods(cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, response.getUrnNbn());
					helper.addIdentifierToDC(cdmId, CDMMetsHelper.DMDSEC_ID_DC_ISSUE, URN_NBN_CODE, response.getUrnNbn(), true);
				} else {
					if (!urnNbnIssueMODS.equals(response.getUrnNbn())) {
						log.debug("Throw systemException: URNNBN identifiers from METS and from database are not equal. DMDSEC_ID_MODS_ISSUE");
						throw new SystemException("URNNBN identifiers from METS and from database are not equal.", ErrorCodes.URNNBN_IS_DIFFERENT_FROM_METS);
					} else {
						log.info("URNNBN DMDSEC_ID_DC_ISSUE identifier is already in METS and URNNBN from database is the same.");
					}
				}
			}

			Node supplementMODS = helper.getDmdSec(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT);
			if (supplementMODS != null) {
				String urnNbnSupplementMODS = helper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT, URN_NBN_CODE);
				if (urnNbnSupplementMODS == null) {
					helper.addValidUrnnbnToMods(cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT, response.getUrnNbn());
					helper.addIdentifierToDC(cdmId, CDMMetsHelper.DMDSEC_ID_DC_SUPPLEMENT, URN_NBN_CODE, response.getUrnNbn(), true);
				} else {
					if (!urnNbnSupplementMODS.equals(response.getUrnNbn())) {
						log.debug("Throw systemException: URNNBN identifiers from METS and from database are not equal. DMDSEC_ID_MODS_SUPPLEMENT");
						throw new SystemException("URNNBN identifiers from METS and from database are not equal.", ErrorCodes.URNNBN_IS_DIFFERENT_FROM_METS);
					} else {
						log.info("URNNBN DMDSEC_ID_MODS_SUPPLEMENT identifier is already in METS and URNNBN from database is the same.");
					}
				}
			}
			checkUrnnbnInMets(cdmId, response.getUrnNbn());

			writeUrnNbnIntoFile(cdmId, response.getUrnNbn());
			log.info("assign finished");
			return response;
		} catch (Exception e) {
			log.error("Unable to add identifier", e);
			throw new SystemException("Unable to add identifier", ErrorCodes.IDENTIFIER_ADDING_FAILED);
		}
	}

	private boolean isImportK4(String cdmId) {
		return "K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"));
	}

	private AssignUrnNbnResponse handleInaccessibleProvider(String cdmId, String registrarCode, AssignUrnNbnResponse response, Exception e) {
		String urnNbn = null;
		// log.error("Inaccessible UrnNbn provider. Fail exception: {}" +
		// e.getClass().getName());
		// log.warn("Assign URN:NBN from resolver failed, using URN:NBN from reserved. Fail message: {}",
		// e.getMessage());
		log.info("Stack trace", e);
		// get URN:NBN from reserved (DB)
		if (urnNbnDao == null) {
			throw new SystemException("URNNBN DB connection not configured", ErrorCodes.INCORRECT_CONFIGURATION);
		}
		try {
			String translatedRegistrarCode = isImportK4(cdmId)?TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping.K4." + registrarCode.toLowerCase()):TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + registrarCode.toLowerCase());
			urnNbn = urnNbnDao.assignUrnNbnFromDb(translatedRegistrarCode, cdmId);
		} catch (DataAccessException ex) {
			log.debug("Throw systemException: Read URN:NBN from database failed.");
			throw new SystemException("Read URN:NBN from database failed", ex, ErrorCodes.URNNBN_RETRIEVING_ERROR);
		}
		if (urnNbn == null) {
			log.debug("Throw systemException: No available URN:NBN in local resource.");
			throw new SystemException("No available URN:NBN in local resource", ErrorCodes.NO_LOCAL_URNNBN);
		}
		response.setUrnNbn(urnNbn);
		response.setUrnNbnSource(UrnNbnSource.DB);
		cdm.updateProperty(cdmId, URN_NBN_SOURCE_CODE, UrnNbnSource.DB.value());

		return response;
	}

	private void writeUrnNbnIntoFile(final String cdmId, final CharSequence urnNbn) {
		checkNotNull(cdmId, "cdmId must not be null");
		checkNotNull(urnNbn, "urnNbn must not be null");

		final CDM cdm = new CDM();
		final File outputFile = new File(cdm.getWorkspaceDir(cdmId), URN_NBN_FILE_NAME);

		try {
			// FileUtils.write(outputFile, urnNbn);
			retriedWrite(outputFile, urnNbn);
		} catch (final IOException e) {
			throw new SystemException(format("Cannot write into file %s", outputFile.getAbsolutePath()), ErrorCodes.ERROR_WHILE_WRITING_FILE);
		}
	}

	public static void main(String[] args) throws CDMException, DocumentException {

		new AssignUrnNbnImpl().assign(null, "e8ddc400-29d8-11e4-8944-00505682629d");
		/*
		 * try { new AssignUrnNbnImpl().checkUrnnbnInMets(
		 * "6db13430-c62b-11e3-a603-00505682629d", "urn:nbn:cz:nk-000141"); }
		 * catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	@RetryOnFailure(attempts = 3)
	private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
		if (params.length > 0) {
			FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
		} else {
			FileUtils.write(file, data, "UTF-8");
		}
	}

	public void checkUrnnbnInMets(String cdmId, String urnnbn) throws Exception {
		log.info("URN:NBN check in METS started ...");

		if (helper.getIdentifierFromMods(cdm, cdmId, helper.getSectionIdMods(cdmId), URN_NBN_CODE) == null) {
			log.debug("URNNBN identifier not found in MODS section of METS file.");
			throw new SystemException("URNNBN identifier not found in MODS section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
		}
		if (!helper.getIdentifiersFromDC(cdm.getMetsFile(cdmId), helper.getSectionIdDC(cdmId)).get("identifier").contains(URN_NBN_CODE + ":" + urnnbn)) {
			log.debug("URNNBN identifier not found in DC section of METS file.");
			throw new SystemException("URNNBN identifier not found in DC section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
		}

		Node issueMODS = helper.getDmdSec(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE);
		if (issueMODS != null) {

			if (helper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, URN_NBN_CODE) == null) {
				log.debug("URNNBN identifier not found in MODS ISSUE section of METS file.");
				throw new SystemException("URNNBN identifier not found in MODS ISSUE section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
			}
			if (!helper.getIdentifiersFromDC(cdm.getMetsFile(cdmId), CDMMetsHelper.DMDSEC_ID_DC_ISSUE).get("identifier").contains(URN_NBN_CODE + ":" + urnnbn)) {
				log.debug("URNNBN identifier not found in DC ISSUE section of METS file.");
				throw new SystemException("URNNBN identifier not found in DC ISSUE section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
			}
		}

		Node supplementMODS = helper.getDmdSec(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT);
		if (supplementMODS != null) {

			if (helper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_SUPPLEMENT, URN_NBN_CODE) == null) {
				log.debug("URNNBN identifier not found in MODS SUPPLEMENT section of METS file.");
				throw new SystemException("URNNBN identifier not found in MODS SUPPLEMENT section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
			}
			if (!helper.getIdentifiersFromDC(cdm.getMetsFile(cdmId), CDMMetsHelper.DMDSEC_ID_DC_SUPPLEMENT).get("identifier").contains(URN_NBN_CODE + ":" + urnnbn)) {
				log.debug("URNNBN identifier not found in DC SUPPLEMENT section of METS file.");
				throw new SystemException("URNNBN identifier not found in DC SUPPLEMENT section of METS file.", ErrorCodes.URNNBN_NOT_FOUND_IN_METS);
			}
		}

		log.info("URN:NBN check in METS finished succesfully");
	}

}
