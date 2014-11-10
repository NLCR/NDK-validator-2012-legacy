package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import com.logica.ndk.tm.utilities.integration.wf.ws.client.wf.WFClient;

/**
 * User: krchnacekm
 */
public abstract class GetUUIDAbstractService {

    protected static final String EMPTY_STRING = "";
    protected final WFClient wfClient;

    protected GetUUIDAbstractService(WFClient wfClient) {
        this.wfClient = wfClient;
    }
}
