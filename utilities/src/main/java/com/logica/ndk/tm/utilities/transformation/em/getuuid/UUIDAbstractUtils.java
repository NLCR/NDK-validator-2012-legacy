package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.integration.wf.task.UUIDResult;
import com.logica.ndk.tm.utilities.transformation.em.GetUUIDImpl;

/**
 * Created with IntelliJ IDEA.
 * User: krchnacekm
 * Date: 20.11.13
 * Time: 10:33
 * To change this template use File | Settings | File Templates.
 */
public abstract class UUIDAbstractUtils {

  private static final Logger log = LoggerFactory.getLogger(GetUUIDImpl.class);
    /**
     * GetUUID utility can return two type of UUID. TitleUUID and VolumeUUID.
     * This method select correct UUID value in record returned from WFClient.
     *
     * @param uuidResult
     *          Value returned from WFClient.
     * @param type
     *          'title' or 'volume'.
     * @return Correct UUID value.
     */
    protected String getCorrectUUIDType(UUIDResult uuidResult, String type) {
        log.info("getCorrectUUIDType started");

        if (UUIDType.TITLE_TYPE.getValue().equalsIgnoreCase(type)) {
            return uuidResult.getTitleUUID();
        }
        else if (UUIDType.VOLUME_TYPE.getValue().equalsIgnoreCase(type)) {
            return uuidResult.getVolumeUUID();
        }
        else {
            throw new IllegalArgumentException(String.format("Illegal value in argument type. Value of argument type is %s, but this value have to be %s or %s", type, UUIDType.TITLE_TYPE.getValue(), UUIDType.VOLUME_TYPE.getValue()));
        }
    }
}
