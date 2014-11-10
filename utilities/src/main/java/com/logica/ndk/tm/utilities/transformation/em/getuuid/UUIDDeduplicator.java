package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.transformation.em.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: krchnacekm
 * Date: 20.11.13
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public final class UUIDDeduplicator extends UUIDAbstractUtils {

    private static final String WFLINK_PATH_PROPERTY_KEY = "jbpmws.wfLink";
    private static final String EMPTY_STRING = "";

    /**
     * GetUUID utility can't return more records with same UUID. This method
     * remove all duplicities by UUID.
     *
     * @param type
     * @param uuiDs
     * @return List of UUIDs without duplicities.
     */
    public List<UUID> removeUUIDDuplicities(String type, List<UUIDResult> uuiDs) {
        if (type != null && uuiDs != null) {
            final Map<String, UUIDResult> result = new HashMap<String, UUIDResult>();

            for (UUIDResult uuid : uuiDs) {
                final String titleOrVolumeUuid = getCorrectUUIDType(uuid, type);
                // If result map doesn't contain record with same UUID. Add this record to the result.
                if (!result.containsKey(titleOrVolumeUuid)) {
                    result.put(titleOrVolumeUuid, uuid);
                }
            }

            return convertUUIDResultsToUUIDs(result);
        }
        else {
            throw new IllegalArgumentException(String.format("All input arguments have to be non null - type: %s, uuiDs: %s", type, uuiDs));
        }
    }

    /**
     * Convert the Map of String and the UUIDResult objects into the List of
     * UUID.
     *
     * @param uuidResults
     * @return List of converted objects.
     */
    private List<UUID> convertUUIDResultsToUUIDs(Map<String, UUIDResult> uuidResults) {
        final List<UUID> result = new ArrayList<UUID>();

        for (String titleOrVolumeUuid : uuidResults.keySet()) {
            UUIDResult foundedUuidResultItem = uuidResults.get(titleOrVolumeUuid);
            String link = createUUIDLink(foundedUuidResultItem);
            result.add(new UUID(titleOrVolumeUuid, foundedUuidResultItem.getSource(), foundedUuidResultItem.getTitle(), link, foundedUuidResultItem.getVolumeNumber()));
        }

        return result;
    }

    /**
     * Create link to the web site, where user can read information about volume.
     *
     * @param uuid
     * @return link
     */
    private String createUUIDLink(UUIDResult uuid) {
        final String link;
        if (uuid.getId() != null) {
            link = String.format("%s%s", TmConfig.instance().getString(WFLINK_PATH_PROPERTY_KEY), uuid.getId());
        }
        else {
            link = EMPTY_STRING;
        }
        return link;
    }
}
