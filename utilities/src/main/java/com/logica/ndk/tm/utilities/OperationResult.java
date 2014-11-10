package com.logica.ndk.tm.utilities;

/**
 * Result of file characterization.
 * 
 * @author Rudolf Daco
 */
public class OperationResult {
    /**
     * Final state - should be the worst state of all executed operations.
     */
    private State state;
    /**
     * All report messages as result of execution of operation.
     */
    private StringBuffer resultMessage;

    private String outputFileName;

    public OperationResult() {
        state = State.OK;
        resultMessage = new StringBuffer();
    }

    public enum State {
        OK, ERROR;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public StringBuffer getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(StringBuffer resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}
