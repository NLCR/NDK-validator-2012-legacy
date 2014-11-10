package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.utilities.integration.wf.UUIDFinder;
import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import java.util.List;

/**
 *
 * @author krchnacekm
 */
public interface GetUUIDService {
    /**
     * Find UUIDs by values in finder.
     *
     * @param finder
     * @return List of founded uuids or empty list
     */
    List<UUIDResult> findUUIDs(UUIDFinder finder);
}
