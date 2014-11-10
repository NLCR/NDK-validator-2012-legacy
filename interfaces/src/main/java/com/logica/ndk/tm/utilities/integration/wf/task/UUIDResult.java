package com.logica.ndk.tm.utilities.integration.wf.task;

public class UUIDResult {

    Long id;
    String titleUUID;
    String volumeUUID;
    String volumeNumber;
    String title;
    String source;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleUUID() {
        return titleUUID;
    }

    public void setTitleUUID(String titleUUID) {
        this.titleUUID = titleUUID;
    }

    public String getVolumeUUID() {
        return volumeUUID;
    }

    public void setVolumeUUID(String volumeUUID) {
        this.volumeUUID = volumeUUID;
    }

    public String getVolumeNumber() {
        return volumeNumber;
    }

    public void setVolumeNumber(String volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "UUIDResult{" + "id=" + id + ", titleUUID=" + titleUUID + ", volumeUUID=" + volumeUUID + ", volumeNumber=" + volumeNumber + ", title=" + title + ", source=" + source + '}';
    }
}
