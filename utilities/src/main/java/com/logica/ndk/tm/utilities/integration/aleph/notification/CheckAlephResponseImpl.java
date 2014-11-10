package com.logica.ndk.tm.utilities.integration.aleph.notification;

import java.io.File;
import java.util.List;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.Record;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.Status;
import com.logica.ndk.tm.utilities.integration.aleph.notification.response.URNNBNNotificationResponse;

/**
 * Kontroluje stav Alpeh notification response v danom CDM. Aleph uz odpovedal a
 * odoslal odpoved a odpoved uz bola prekopirovana do spravneho CDM (urobila to
 * utilita ReadAlephResponseImpl). Tato utilta uz len kontroluje odpoved v danom
 * CDM.
 *
 * @author Rudolf Daco
 */
public class CheckAlephResponseImpl extends AbstractUtility {

     /**
     * Skontroluje ci uz existuje v CDM subor s Aleph notification response. Ak
     * ano, odpoved sa precita a ak je odpovede OK tak return
     * CHECK_ALEPH_RESPONSE_STATUS_OK ak odpoved je ERROR tak exception ak este
     * subor neexistuje tak return CHECK_ALEPH_RESPONSE_STATUS_WAIT.
     *
     * @param cdmId
     * @return
     */
    public String execute(String cdmId, String locality) {
        String status = AlephNotificationConstants.CHECK_ALEPH_RESPONSE_STATUS_WAIT;
        File responseFile = getResponseFile(cdmId, locality);
        if (responseFile != null) {
            URNNBNNotificationResponse response = AlephNotificationHelper.readResponse(responseFile);
            List<Record> record = response.getRecords().getRecord();
            if (record.size() == 1) {
                Record aRecord = record.get(0);
                if (Status.OK.equals(aRecord.getStatus())) {
                    status = AlephNotificationConstants.CHECK_ALEPH_RESPONSE_STATUS_OK;
                } else if (Status.ERROR.equals(aRecord.getStatus())) {
                    throw new AlephNotificationException("Aleph notification response status is: " + Status.ERROR + " Reason is: " + aRecord.getReason(), ErrorCodes.CHECK_ALEPH_RESPONSE);
                } else {
                    throw new SystemException("Incorrect response file: " + responseFile.getAbsolutePath() + " Incorrect format of status: " + aRecord.getStatus(), ErrorCodes.INCORRECT_ALEPH_RESPONSE);
                }
            } else {
                throw new SystemException("Incorrect response file: " + responseFile.getAbsolutePath() + " Response file for CDM has to contain 1 record.", ErrorCodes.INCORRECT_ALEPH_RESPONSE);
            }
        }
        return status;
    }

    private File getResponseFile(String cdmId, String locality) {
        File responseFile = null;
        File dir = new CDM().getAlephNotificationResponseDir(cdmId);
        if (dir.exists() == true) {
            File file = new File(dir, cdmId + ".xml");
            if (file.exists()) {
                return file;
            } else {
                throw new SystemException("There is more than 1 Aleph notification response id direcotry: " + dir.getAbsolutePath(), ErrorCodes.INCORRECT_ALEPH_RESPONSE);
            }            
        }
        return responseFile;
    }


}
