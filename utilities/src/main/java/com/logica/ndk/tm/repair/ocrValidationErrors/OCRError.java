package com.logica.ndk.tm.repair.ocrValidationErrors;

/**
 * Created with IntelliJ IDEA.
 * User: krchnacekm
 * Date: 28.11.13
 */
class OCRError {
    private String cdmId;
    private String fileName;
    private String userName;
    private String error;

    String getCdmId() {
        return cdmId;
    }

    void setCdmId(final String cdmId) {
        this.cdmId = cdmId;
    }


    String getFileName() {
        return fileName;
    }

    void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    String getUserName() {
        return userName;
    }

    void setUserName(final String userName) {
        this.userName = userName;
    }

    String getError() {
        return error;
    }

    void setError(final String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "OCRError{" +
                "cdmId='" + cdmId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", userName='" + userName + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
