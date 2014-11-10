package com.logica.ndk.tm.utilities.transformation.sip2;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMSchema;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.transformation.sip2.exception.KrameriusProcessFailedException;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author brizat
 */
public class CheckKrameriusProcessResultImpl extends CheckKrameriusProcess {

    public String execute(String processId, String locality, String cdmId) throws BusinessException, SystemException, IOException {
        initializeStrings(locality);

        Preconditions.checkNotNull(processId);

        HttpClientImpl httpClient = new HttpClientImpl();
        httpClient.setContentType("application/json");
        HttpResponse response = httpClient.doGet(URL.replace("${procesId}", processId), null, USER, PASSWORD);

        log.info("Result code: " + response.getReponseStatus() + ", response body: " + response.getResponseBody());

        if (response.getReponseStatus() == 200 || response.getReponseStatus() == 201 || response.getReponseStatus() == 202) {

        //parsing JSON response
            String jsonStr = response.getResponseBody();

            // getting root node and bacthstate Node
            String globalState = JsonPath.read(jsonStr, "[0].state");
            String batchState = JsonPath.read(jsonStr, "[0].batchState");
            List<Object> childrens = null;
            try {
                childrens = JsonPath.read(jsonStr, "[0].children[*]");
            } catch (PathNotFoundException ie) {
                log.info("No children process found.");
            }
            if (childrens != null && childrens.size() > 0) {
                //batch process
                if (Strings.isNullOrEmpty(batchState)) {
                    return BATCH_STATE_RUNNING_VALUE;
                }
                if (batchState.equalsIgnoreCase(BATCH_STATE_FINISHED_VALUE)) {
                    return BATCH_STATE_FINISHED_VALUE;
                }
                if (batchState.equalsIgnoreCase(BATCH_STATE_RUNNING_VALUE) || batchState.equalsIgnoreCase(BATCH_STATE_STARTED_VALUE)) {
                    return BATCH_STATE_RUNNING_VALUE;
                }

                if (batchState.equalsIgnoreCase(BATCH_STATE_WARNING_VALUE)) {
                    if (Boolean.valueOf(REPORT_WARNINGS)) {
                        if (WARNING_VALUE.equalsIgnoreCase(getImportProcessStatus(jsonStr))) {
                            throw new KrameriusProcessFailedException("The import process finished with warning in Kramerius", ErrorCodes.CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_WARNED);
                        } else if (WARNING_VALUE.equalsIgnoreCase(getIndexProcessStatus(jsonStr))) {
                            createImportDoneFlag(cdmId, locality);
                            throw new KrameriusProcessFailedException("The index process finished with warning in Kramerius", ErrorCodes.CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_WARNED);
                        } else {
                            throw new SystemException("Could not evaluate process in warning state");
                        }
                    } else {
                        return BATCH_STATE_FINISHED_VALUE;
                    }
                }
                if (batchState.equalsIgnoreCase(BATCH_STATE_FAILURE_VALUE)) {
                    if (FAILURE_VALUE.equalsIgnoreCase(getImportProcessStatus(jsonStr))) {
                        new ClearKrameriusFolderImpl().execute(cdmId, locality, TmConfig.instance().getString("utility.sip2.profile." + locality + ".move.target"));
                        throw new KrameriusProcessFailedException("The import process failed in Kramerius", ErrorCodes.CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_FAILED);
                    } else if (FAILURE_VALUE.equalsIgnoreCase(getIndexProcessStatus(jsonStr))) {
                        createImportDoneFlag(cdmId, locality);
                        throw new KrameriusProcessFailedException("The index process failed in Kramerius", ErrorCodes.CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_FAILED);
                    } else {
                        throw new SystemException("Could not evaluate process in failed state");
                    }

                }
                //process running
                return BATCH_STATE_RUNNING_VALUE;
            } else {
                if (Strings.isNullOrEmpty(globalState)) {
                    return BATCH_STATE_RUNNING_VALUE;
                }
                if (FINISHED_VALUE.equalsIgnoreCase(globalState)) {
                    return BATCH_STATE_FINISHED_VALUE;
                }
                
                //no batch process
                String def = JsonPath.read(jsonStr, "[0].def");
                if (globalState.equalsIgnoreCase(WARNING_VALUE)) {
                    if (Boolean.valueOf(REPORT_WARNINGS)) {
                        Long errorCode = ErrorCodes.KRAMERIUS_PROCESS_END_WITH_WARNING;
                        if (REINDEX_VALUE.equalsIgnoreCase(def)) {
                            errorCode = ErrorCodes.CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_WARNED;
                        } else if (PARAM_IMPORT_VALUE.equalsIgnoreCase(def)) {
                            errorCode = ErrorCodes.CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_WARNED;
                        }
                        throw new KrameriusProcessFailedException("The " + def + " process finished with warning in Kramerius", errorCode);
                    } else {
                        return BATCH_STATE_FINISHED_VALUE;
                    }
                }
                if (globalState.equalsIgnoreCase(FAILURE_VALUE)) {
                    //default error code
                    Long errorCode = ErrorCodes.KRAMERIUS_PROCESS_FAILED;
                    if (REINDEX_VALUE.equalsIgnoreCase(def)) {
                        errorCode = ErrorCodes.CHECK_KRAMERIUS_INDEX_PROCESS_RESULT_FAILED;
                    } else if (PARAM_IMPORT_VALUE.equalsIgnoreCase(def)) {
                        errorCode = ErrorCodes.CHECK_KRAMERIUS_IMPORT_PROCESS_RESULT_FAILED;
                    }
                    throw new KrameriusProcessFailedException("The " + def + " process failed in Kramerius", errorCode);
                }
                //process running
                return BATCH_STATE_RUNNING_VALUE;

            }

        } else {
            throw new KrameriusProcessFailedException("Get process state failed, http status code: " + response.getReponseStatus() + ", body: " + response.getResponseBody(), ErrorCodes.CHECK_KRAMERIUS_PROCESS_RESULT_FAILED);
        }
    }

    private String getImportProcessStatus(String jsonStr) {
        return JsonPath.read(jsonStr, "[0].children[?].state[0]", Filter.filter(Criteria.where(DEF_PROPERTY).is(PARAM_IMPORT_VALUE)));
    }

    private String getIndexProcessStatus(String jsonStr) {
        return JsonPath.read(jsonStr, "[0].children[?].state[0]", Filter.filter(Criteria.where(DEF_PROPERTY).is(REINDEX_VALUE)));
    }

    @RetryOnFailure(types = IOException.class)
    private void createImportDoneFlag(String cdmId, String locality) throws IOException {
        File importK4DoneFile = new File(cdm.getWorkspaceDir(cdmId), CDMSchema.CDMSchemaDir.IMPORT_K4_FINISH_OK + locality);
        if (!importK4DoneFile.exists()) {
            importK4DoneFile.createNewFile();
        }
    }

    public static void main(String[] args) throws IOException {
        new CheckKrameriusProcessResultImpl().execute(DEF_PROPERTY, DEF_PROPERTY, DEF_PROPERTY);
    }

}
