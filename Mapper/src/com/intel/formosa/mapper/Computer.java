package com.intel.formosa.mapper;

import java.util.ArrayList;

public class Computer {
	private String IP;
	private String taskSessionId;
	
	public Computer(String IP) {
		this.IP = IP;
	}
	
	public String getIP() {
		return IP;
	}
	
	public String getTaskSessionId () {
		return taskSessionId;
	}
	
	
	public void setTaskSessionId (String sessionId) {
		taskSessionId = sessionId;
	}
}
