package com.logica.ndk.tm.utilities.transformation.em.getuuid;

/**
 * User: krchnacekm
 */
public enum UUIDType {

    VOLUME_TYPE("volume"), TITLE_TYPE("title");

    private String value;

    private UUIDType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
