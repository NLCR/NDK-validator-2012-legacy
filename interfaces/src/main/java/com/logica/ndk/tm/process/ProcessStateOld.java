package com.logica.ndk.tm.process;

import java.io.Serializable;

/**
 * State of process.
 * 
 * @author Rudolf Daco
 */
@Deprecated
public class ProcessStateOld implements Serializable {
	private static final long serialVersionUID = 1321613514018480975L;
	private String processId;
	private String processName;
	private long instanceId;
	/**
	 * STATE_PENDING = 0; STATE_ACTIVE = 1; STATE_COMPLETED = 2; STATE_ABORTED =
	 * 3; STATE_SUSPENDED = 4;
	 */
	private int state;
	private ParamMap paramMap;

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public ParamMap getParamMap() {
		return paramMap;
	}

	public void setParamMap(ParamMap paramMap) {
		this.paramMap = paramMap;
	}
}
