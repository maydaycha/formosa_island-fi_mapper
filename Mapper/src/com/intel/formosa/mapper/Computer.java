package com.intel.formosa.mapper;


public class Computer {
	private String IP;
	private String taskSessionId;
	
	public Computer(String IP) {
		this.IP = IP;
	}

    /** Getter */
	public String getIP() {
		return IP;
	}
	
	public String getTaskSessionId () {
		return taskSessionId;
	}

    /** Setter */
	public void setTaskSessionId (String sessionId) {
		taskSessionId = sessionId;
	}
}
